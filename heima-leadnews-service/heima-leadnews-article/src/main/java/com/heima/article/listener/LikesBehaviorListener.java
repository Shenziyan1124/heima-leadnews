package com.heima.article.listener;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.mapper.ApLikesBehaviorMapper;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApLikesBehavior;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class LikesBehaviorListener {

    @Autowired
    private ApLikesBehaviorMapper apLikesBehaviorMapper;
    @Autowired
    private ApArticleMapper apArticleMapper;

    @KafkaListener(topics = ApUserBehaviorConstants.LIKES_KAFKA_TOPIC)
    public void onMessage(String message) {
        log.info("article端收到likes Kafka的消息: {}", message);

        try {
            if (StringUtils.isNotBlank(message)) {
                Map map = JSON.parseObject(message, Map.class);

                Integer userId = ((Number) map.get("userId")).intValue();
                Long articleId = ((Number) map.get("articleId")).longValue();
                Short type = ((Number) map.get("type")).shortValue();
                Short operation = ((Number) map.get("operation")).shortValue();

                if (Objects.equals(operation, ApUserBehaviorConstants.LIKE)) {
                    handleLike(userId, articleId, type);
                } else {
                    handleCancelLike(userId, articleId, type);
                }
            }
        } catch (Exception e) {
            log.error("Kafka消费点赞消息失败，等待重试: {}", e.getMessage());
            throw e;
        }
    }

    private void handleLike(Integer userId, Long articleId, Short type) {
        LambdaQueryWrapper<ApLikesBehavior> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApLikesBehavior::getArticleId, articleId);
        queryWrapper.eq(ApLikesBehavior::getUserId, userId);
        queryWrapper.eq(ApLikesBehavior::getType, type);
        queryWrapper.eq(ApLikesBehavior::getOperation, ApUserBehaviorConstants.LIKE);
        ApLikesBehavior existRecord = apLikesBehaviorMapper.selectOne(queryWrapper);

        if (existRecord == null) {
            try {
                ApLikesBehavior record = new ApLikesBehavior();
                record.setUserId(userId);
                record.setArticleId(articleId);
                record.setType(type);
                record.setOperation(ApUserBehaviorConstants.LIKE);
                record.setCreatedTime(new Date());
                record.setUpdateTime(new Date());
                record.setIsDelete(ApUserBehaviorConstants.CANCEL_DELETE);
                apLikesBehaviorMapper.insert(record);
                log.info("article端插入点赞记录成功: articleId={}, userId={}", articleId, userId);
            } catch (DuplicateKeyException e) {
                log.warn("article端点赞记录已存在(并发插入), 跳过: articleId={}, userId={}", articleId, userId);
            }
        } else if (existRecord.getIsDelete() == ApUserBehaviorConstants.DELETE) {
            LambdaUpdateWrapper<ApLikesBehavior> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ApLikesBehavior::getId, existRecord.getId());
            updateWrapper.set(ApLikesBehavior::getIsDelete, ApUserBehaviorConstants.CANCEL_DELETE);
            updateWrapper.set(ApLikesBehavior::getUpdateTime, new Date());
            apLikesBehaviorMapper.update(null, updateWrapper);
            log.info("article端恢复点赞记录成功: articleId={}, userId={}", articleId, userId);
        } else {
            log.info("article端点赞记录已存在，跳过: articleId={}, userId={}", articleId, userId);
        }

        LambdaUpdateWrapper<ApArticle> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ApArticle::getId, articleId);
        updateWrapper.setSql("likes = likes + 1");
        apArticleMapper.update(null, updateWrapper);
    }

    private void handleCancelLike(Integer userId, Long articleId, Short type) {
        LambdaQueryWrapper<ApLikesBehavior> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApLikesBehavior::getArticleId, articleId);
        queryWrapper.eq(ApLikesBehavior::getUserId, userId);
        queryWrapper.eq(ApLikesBehavior::getType, type);
        queryWrapper.eq(ApLikesBehavior::getOperation, ApUserBehaviorConstants.LIKE);
        ApLikesBehavior existRecord = apLikesBehaviorMapper.selectOne(queryWrapper);

        if (existRecord == null || existRecord.getIsDelete() == ApUserBehaviorConstants.DELETE) {
            log.info("article端取消点赞记录不存在或已删除, 跳过: articleId={}, userId={}", articleId, userId);
            return;
        }

        LambdaUpdateWrapper<ApLikesBehavior> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ApLikesBehavior::getId, existRecord.getId());
        updateWrapper.set(ApLikesBehavior::getIsDelete, ApUserBehaviorConstants.DELETE);
        updateWrapper.set(ApLikesBehavior::getUpdateTime, new Date());
        apLikesBehaviorMapper.update(null, updateWrapper);

        LambdaUpdateWrapper<ApArticle> updateWrapper2 = new LambdaUpdateWrapper<>();
        updateWrapper2.eq(ApArticle::getId, articleId);
        updateWrapper2.setSql("likes = GREATEST(IFNULL(likes, 0) - 1, 0)");
        apArticleMapper.update(null, updateWrapper2);

        log.info("article端取消点赞成功: articleId={}, userId={}", articleId, userId);
    }
}
