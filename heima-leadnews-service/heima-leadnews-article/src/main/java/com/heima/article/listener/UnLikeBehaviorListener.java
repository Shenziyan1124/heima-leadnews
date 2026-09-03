package com.heima.article.listener;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.article.mapper.ApLikesBehaviorMapper;
import com.heima.common.constants.ApUserBehaviorConstants;
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
public class UnLikeBehaviorListener {

    @Autowired
    private ApLikesBehaviorMapper apLikesBehaviorMapper;

    @KafkaListener(topics = ApUserBehaviorConstants.UN_LIKE_KAFKA_TOPIC)
    public void onMessage(String message) {
        log.info("article端收到unLike Kafka的消息: {}", message);

        try {
            if (StringUtils.isNotBlank(message)) {
                Map map = JSON.parseObject(message, Map.class);

                Integer userId = ((Number) map.get("userId")).intValue();
                Long articleId = ((Number) map.get("articleId")).longValue();
                Short type = ((Number) map.get("type")).shortValue();
                Short operation = ((Number) map.get("operation")).shortValue();

                if (Objects.equals(operation, ApUserBehaviorConstants.UN_LIKE)) {
                    handleUnLike(userId, articleId, type);
                } else {
                    handleCancelUnLike(userId, articleId, type);
                }
            }
        } catch (Exception e) {
            log.error("Kafka消费不喜欢消息失败，等待重试: {}", e.getMessage());
            throw e;
        }
    }

    private void handleUnLike(Integer userId, Long articleId, Short type) {
        LambdaQueryWrapper<ApLikesBehavior> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApLikesBehavior::getArticleId, articleId);
        queryWrapper.eq(ApLikesBehavior::getUserId, userId);
        queryWrapper.eq(ApLikesBehavior::getType, type);
        queryWrapper.eq(ApLikesBehavior::getOperation, ApUserBehaviorConstants.UN_LIKE);
        ApLikesBehavior existRecord = apLikesBehaviorMapper.selectOne(queryWrapper);

        if (existRecord == null) {
            try {
                ApLikesBehavior record = new ApLikesBehavior();
                record.setArticleId(articleId);
                record.setUserId(userId);
                record.setType(type);
                record.setOperation(ApUserBehaviorConstants.UN_LIKE);
                record.setCreatedTime(new Date());
                record.setUpdateTime(new Date());
                record.setIsDelete(ApUserBehaviorConstants.CANCEL_DELETE);
                apLikesBehaviorMapper.insert(record);
                log.info("article端插入不喜欢记录成功: articleId={}, userId={}", articleId, userId);
            } catch (DuplicateKeyException e) {
                log.warn("article端不喜欢记录已存在(并发插入), 跳过: articleId={}, userId={}", articleId, userId);
            }
        } else if (existRecord.getIsDelete() == ApUserBehaviorConstants.DELETE) {
            LambdaUpdateWrapper<ApLikesBehavior> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ApLikesBehavior::getId, existRecord.getId());
            updateWrapper.set(ApLikesBehavior::getIsDelete, ApUserBehaviorConstants.CANCEL_DELETE);
            updateWrapper.set(ApLikesBehavior::getUpdateTime, new Date());
            apLikesBehaviorMapper.update(null, updateWrapper);
            log.info("article端恢复不喜欢记录成功: articleId={}, userId={}", articleId, userId);
        } else {
            log.info("article端不喜欢记录已存在，跳过: articleId={}, userId={}", articleId, userId);
        }
    }

    private void handleCancelUnLike(Integer userId, Long articleId, Short type) {
        LambdaUpdateWrapper<ApLikesBehavior> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ApLikesBehavior::getArticleId, articleId);
        updateWrapper.eq(ApLikesBehavior::getUserId, userId);
        updateWrapper.eq(ApLikesBehavior::getType, type);
        updateWrapper.eq(ApLikesBehavior::getOperation, ApUserBehaviorConstants.UN_LIKE);
        updateWrapper.set(ApLikesBehavior::getIsDelete, ApUserBehaviorConstants.DELETE);
        updateWrapper.set(ApLikesBehavior::getUpdateTime, new Date());
        apLikesBehaviorMapper.update(null, updateWrapper);
        log.info("article端软删除不喜欢记录成功: articleId={}, userId={}", articleId, userId);
    }
}
