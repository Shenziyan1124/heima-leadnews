package com.heima.article.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ApLikesBehaviorMapper;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.pojos.ApLikesBehavior;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 定时对账：Redis不喜欢数据 → MySQL同步
 */
@Component
@Slf4j
public class UnLikeDataSyncSchedule {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ApLikesBehaviorMapper likesBehaviorMapper;

    /**
     * 每5分钟执行一次，同步Redis不喜欢数据到MySQL
     */
    @Scheduled(fixedRate = 300000, initialDelay = 10000)
    public void syncUnLikeData() {
        log.info("开始同步Redis不喜欢数据到MySQL...");

        // 1. 扫描所有 un_like:article:* 的key
        Set<String> keys = cacheService.scan(ApUserBehaviorConstants.UN_LIKE_ARTICLE_KEY + "*");

        if (keys == null || keys.isEmpty()) {
            log.info("Redis中没有不喜欢数据，跳过同步");
            return;
        }

        int syncCount = 0;

        for (String key : keys) {
            // key格式: un_like:article:{articleId}
            String articleIdStr = key.substring(ApUserBehaviorConstants.UN_LIKE_ARTICLE_KEY.length());
            Long articleId;
            try {
                articleId = Long.parseLong(articleIdStr);
            } catch (NumberFormatException e) {
                log.warn("解析articleId失败: {}", articleIdStr);
                continue;
            }

            // 2. 获取该文章在Redis中的所有不喜欢用户（使用sScan）
            Set<String> userIds = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions().match("*").count(100).build();
            try (Cursor<String> cursor = cacheService.sScan(key, options)) {
                while (cursor.hasNext()) {
                    userIds.add(cursor.next());
                }
            }

            if (userIds.isEmpty()) {
                continue;
            }

            // 3. 查询MySQL中该文章已有的不喜欢记录（未删除的）
            LambdaQueryWrapper<ApLikesBehavior> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ApLikesBehavior::getArticleId, articleId);
            wrapper.eq(ApLikesBehavior::getOperation, ApUserBehaviorConstants.UN_LIKE); // operation=2不喜欢
            wrapper.eq(ApLikesBehavior::getIsDelete, (short) 0);
            List<ApLikesBehavior> existList = likesBehaviorMapper.selectList(wrapper);

            // 已存在的userId集合
            Set<Integer> existUserIds = new HashSet<>();
            for (ApLikesBehavior behavior : existList) {
                existUserIds.add(behavior.getUserId());
            }

            // 4. 找出Redis中有但MySQL中没有的记录，补插入
            for (String userIdStr : userIds) {
                try {
                    Integer userId = Integer.parseInt(userIdStr);
                    if (!existUserIds.contains(userId)) {
                        ApLikesBehavior newRecord = new ApLikesBehavior();
                        newRecord.setArticleId(articleId);
                        newRecord.setUserId(userId);
                        newRecord.setType((short) 0); // 默认文章
                        newRecord.setOperation(ApUserBehaviorConstants.UN_LIKE); // operation=2不喜欢
                        newRecord.setCreatedTime(new Date());
                        newRecord.setUpdateTime(new Date());
                        newRecord.setIsDelete(ApUserBehaviorConstants.CANCEL_DELETE); // 默认未删除
                        try {
                            likesBehaviorMapper.insert(newRecord);
                            syncCount++;
                            log.info("补插入不喜欢记录: articleId={}, userId={}", articleId, userId);
                        } catch (DuplicateKeyException e) {
                            log.warn("不喜欢记录已存在(并发插入), 跳过: articleId={}, userId={}", articleId, userId);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.warn("解析userId失败: {}", userIdStr);
                }
            }
        }

        log.info("Redis不喜欢数据同步完成，共补插入{}条记录", syncCount);
    }
}
