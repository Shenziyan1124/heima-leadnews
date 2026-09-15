package com.heima.article.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.mapper.ApCollectionMapper;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApCollection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 定时对账：Redis收藏数据 → MySQL同步
 */
@Component
@Slf4j
public class CollectionDataSyncSchedule {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ApCollectionMapper apCollectionMapper;

    @Autowired
    private ApArticleMapper apArticleMapper;

    /**
     * 每5分钟执行一次，同步Redis收藏数据到MySQL
     */
    @Scheduled(fixedRate = 300000, initialDelay = 30000)
    public void syncCollectionData() throws IOException {
        log.info("开始同步Redis收藏数据到MySQL...");

        // 1. 扫描所有 collect:article:* 的key
        Set<String> keys = cacheService.scan(ApUserBehaviorConstants.COLLECT_ARTICLE_KEY + "*");

        if (keys == null || keys.isEmpty()) {
            log.info("Redis中没有收藏数据，跳过同步");
            return;
        }

        int syncCount = 0;

        for (String key : keys) {
            // key格式: collect:article:{articleId}
            String articleIdStr = key.substring(ApUserBehaviorConstants.COLLECT_ARTICLE_KEY.length());
            Long articleId;
            try {
                articleId = Long.parseLong(articleIdStr);
            } catch (NumberFormatException e) {
                log.warn("解析articleId失败: {}", articleIdStr);
                continue;
            }

            // 2. 获取该文章在Redis中的所有收藏用户（使用sScan）
            Set<String> userIds = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions().match("*").count(100).build();
            try (Cursor<String> cursor = cacheService.sScan(key, options)) {
                while (cursor.hasNext()) {
                    userIds.add(cursor.next());
                }
            }

            // 3. 查询MySQL中该文章已有的收藏记录（未删除的）
            LambdaQueryWrapper<ApCollection> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ApCollection::getArticleId, articleId);
            wrapper.eq(ApCollection::getIsDelete, (short) 0);
            List<ApCollection> existList = apCollectionMapper.selectList(wrapper);

            // 已存在的userId集合
            Set<Integer> existUserIds = new HashSet<>();
            for (ApCollection collection : existList) {
                existUserIds.add(collection.getUserId());
            }

            // 4. 找出Redis中有但MySQL中没有的记录，补插入
            for (String userIdStr : userIds) {
                try {
                    Integer userId = Integer.parseInt(userIdStr);
                    if (!existUserIds.contains(userId)) {
                        ApCollection newRecord = new ApCollection();
                        newRecord.setArticleId(articleId);
                        newRecord.setUserId(userId);
                        newRecord.setType((short) 0); // 默认文章
                        newRecord.setIsDelete((short) 0);
                        newRecord.setCollectionTime(new Date());
                        newRecord.setPublishedTime(new Date());
                        try {
                            apCollectionMapper.insert(newRecord);
                            syncCount++;
                            log.info("补插入收藏记录: articleId={}, userId={}", articleId, userId);
                        } catch (DuplicateKeyException e) {
                            log.warn("收藏记录已存在(并发插入), 跳过: articleId={}, userId={}", articleId, userId);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.warn("解析userId失败: {}", userIdStr);
                }
            }

            // 5. 同步ap_article.collection（Redis Set数量 = 收藏数）
            LambdaQueryWrapper<ApArticle> articleWrapper = new LambdaQueryWrapper<>();
            articleWrapper.eq(ApArticle::getId, articleId);
            articleWrapper.select(ApArticle::getId, ApArticle::getCollection);
            ApArticle article = apArticleMapper.selectOne(articleWrapper);

            if (article != null) {
                int redisCollection = userIds.size();
                Integer mysqlCollection = article.getCollection();

                // 只在值不同时更新
                if (mysqlCollection == null || mysqlCollection != redisCollection) {
                    LambdaUpdateWrapper<ApArticle> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(ApArticle::getId, articleId);
                    updateWrapper.set(ApArticle::getCollection, redisCollection);
                    apArticleMapper.update(null, updateWrapper);
                    log.info("同步文章收藏数成功: articleId={}, redis={}, mysql={}", articleId, redisCollection, mysqlCollection);
                }
            }
        }

        log.info("Redis收藏数据同步完成，共补插入{}条记录", syncCount);
    }
}
