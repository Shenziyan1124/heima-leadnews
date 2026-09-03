package com.heima.article.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.mapper.ApLikesBehaviorMapper;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApLikesBehavior;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 定时对账：Redis点赞数据 → MySQL同步
 */
@Component
@Slf4j
public class LikesDataSyncSchedule {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ApLikesBehaviorMapper likesBehaviorMapper;

    @Autowired
    private ApArticleMapper apArticleMapper;

    /**
     * 每5分钟执行一次，同步Redis点赞数据到MySQL
     */
    @Scheduled(fixedRate = 300000)
    public void syncLikesData() {
        log.info("开始同步Redis点赞数据到MySQL...");

        // 1. 扫描所有 likes:article:* 的key
        Set<String> keys = cacheService.scan(ApUserBehaviorConstants.LIKES_ARTICLE_KEY + "*");

        if (keys == null || keys.isEmpty()) {
            log.info("Redis中没有点赞数据，跳过同步");
            return;
        }

        int syncCount = 0;

        for (String key : keys) {
            // key格式: likes:article:{articleId}
            String articleIdStr = key.substring(ApUserBehaviorConstants.LIKES_ARTICLE_KEY.length());
            Long articleId;
            try {
                articleId = Long.parseLong(articleIdStr);
            } catch (NumberFormatException e) {
                log.warn("解析articleId失败: {}", articleIdStr);
                continue;
            }

            // 2. 获取该文章在Redis中的所有点赞用户
            Set<String> userIds = cacheService.setMembers(key);
            if (userIds == null || userIds.isEmpty()) {
                continue;
            }

            // 3. 查询MySQL中该文章已有的点赞记录（未删除的）
            LambdaQueryWrapper<ApLikesBehavior> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ApLikesBehavior::getArticleId, articleId);
            wrapper.eq(ApLikesBehavior::getType, ApUserBehaviorConstants.LIKES_ARTICLE_TYPE);
            wrapper.eq(ApLikesBehavior::getIsDelete, (short) 0);
            List<ApLikesBehavior> existList = likesBehaviorMapper.selectList(wrapper);

            // 已存在的userId集合
            Set<Integer> existUserIds = new java.util.HashSet<>();
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
                        newRecord.setType(ApUserBehaviorConstants.LIKES_ARTICLE_TYPE);
                        newRecord.setCreatedTime(new Date());
                        newRecord.setUpdateTime(new Date());
                        newRecord.setIsDelete(ApUserBehaviorConstants.CANCEL_DELETE);
                        likesBehaviorMapper.insert(newRecord);
                        syncCount++;
                        log.info("补插入点赞记录: articleId={}, userId={}", articleId, userId);
                    }
                } catch (NumberFormatException e) {
                    log.warn("解析userId失败: {}", userIdStr);
                }
            }

            // 5. 同步ap_article.likes（Redis Set数量 = 点赞数）
            LambdaQueryWrapper<ApArticle> articleWrapper = new LambdaQueryWrapper<>();
            articleWrapper.eq(ApArticle::getId, articleId);
            articleWrapper.select(ApArticle::getId, ApArticle::getLikes);
            ApArticle article = apArticleMapper.selectOne(articleWrapper);

            if (article != null) {
                int redisLikes = userIds.size();
                Integer mysqlLikes = article.getLikes();

                // 只在值不同时更新
                if (mysqlLikes == null || mysqlLikes != redisLikes) {
                    LambdaUpdateWrapper<ApArticle> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(ApArticle::getId, articleId);
                    updateWrapper.set(ApArticle::getLikes, redisLikes);
                    apArticleMapper.update(null, updateWrapper);
                    log.info("同步文章点赞数成功: articleId={}, redis={}, mysql={}", articleId, redisLikes, mysqlLikes);
                }
            }
        }

        log.info("Redis点赞数据同步完成，共补插入{}条记录", syncCount);
    }
}
