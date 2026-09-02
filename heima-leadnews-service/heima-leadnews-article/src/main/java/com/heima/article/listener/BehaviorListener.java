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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class BehaviorListener {

    @Autowired
    private ApLikesBehaviorMapper apLikesBehaviorMapper;

    @KafkaListener(topics = ApUserBehaviorConstants.LIKES_KAFKA_TOPIC)
    public void likesBehaviorListener(String message) {
        log.info("article端收到behavior Kafka的消息: {}", message);

        try {
            if (StringUtils.isNotBlank(message)){
                Map map = JSON.parseObject(message, Map.class);

                Integer userId = ((Number) map.get("userId")).intValue();
                Long articleId = ((Number) map.get("articleId")).longValue();
                Short type = ((Number) map.get("type")).shortValue();
                Short operation = ((Number) map.get("operation")).shortValue();

                if (Objects.equals(operation, ApUserBehaviorConstants.LIKE)) {
                    // 点赞 - 插入数据库
                    ApLikesBehavior apLikesBehavior = new ApLikesBehavior();
                    apLikesBehavior.setUserId(userId);
                    apLikesBehavior.setArticleId(articleId);
                    apLikesBehavior.setType(type);
                    apLikesBehavior.setCreatedTime(new Date());
                    apLikesBehavior.setUpdateTime(new Date());
                    apLikesBehavior.setIsDelete(ApUserBehaviorConstants.UN_DELETE);
                    apLikesBehaviorMapper.insert(apLikesBehavior);
                    log.info("article端保存用户点赞行为数据成功: {}", apLikesBehavior);
                } else {
                    // 取消点赞 - 软删除（更新is_delete=1）
                    LambdaUpdateWrapper<ApLikesBehavior> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(ApLikesBehavior::getArticleId, articleId);
                    updateWrapper.eq(ApLikesBehavior::getUserId, userId);
                    updateWrapper.eq(ApLikesBehavior::getType, type);
                    updateWrapper.set(ApLikesBehavior::getIsDelete, ApUserBehaviorConstants.DELETE);
                    updateWrapper.set(ApLikesBehavior::getUpdateTime, new Date());
                    apLikesBehaviorMapper.update(null, updateWrapper);
                    log.info("article端软删除用户点赞行为数据成功: articleId={}, userId={}", articleId, userId);
                }
            }
        } catch (Exception e) {
            log.error("Kafka消费点赞消息失败，等待重试: {}", e.getMessage());
            throw e; // 抛出异常，Kafka会自动重试
        }
    }
}
