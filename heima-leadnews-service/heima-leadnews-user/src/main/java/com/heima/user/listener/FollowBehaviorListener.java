package com.heima.user.listener;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.model.user.pojos.ApUserFollow;
import com.heima.user.mapper.ApUserFollowMapper;
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
public class FollowBehaviorListener {

    @Autowired
    private ApUserFollowMapper apUserFollowMapper;

    @KafkaListener(topics = ApUserBehaviorConstants.FOLLOW_KAFKA_TOPIC)
    public void onMessage(String message) {
        log.info("user端收到follow Kafka的消息: {}", message);

        try {
            if (StringUtils.isNotBlank(message)) {
                Map map = JSON.parseObject(message, Map.class);

                Integer userId = ((Number) map.get("userId")).intValue();
                Integer followId = ((Number) map.get("followId")).intValue();
                String followName = (String) map.get("followName");
                Short operation = ((Number) map.get("operation")).shortValue();

                if (Objects.equals(operation, ApUserBehaviorConstants.FOLLOW)) {
                    handleFollow(userId, followId, followName);
                } else {
                    handleCancelFollow(userId, followId);
                }
            }
        } catch (Exception e) {
            log.error("Kafka消费关注消息失败，等待重试: {}", e.getMessage());
            throw e;
        }
    }

    private void handleFollow(Integer userId, Integer followId, String followName) {
        // 查询是否已存在关注关系
        QueryWrapper<ApUserFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("follow_id", followId);
        ApUserFollow existRecord = apUserFollowMapper.selectOne(queryWrapper);

        if (existRecord == null) {
            try {
                ApUserFollow record = new ApUserFollow();
                record.setUserId(userId);
                record.setFollowId(followId);
                record.setFollowName(followName);
                record.setLevel((short) 0);
                record.setIsNotice((short) 0);
                record.setCreatedTime(new Date());
                apUserFollowMapper.insert(record);
                log.info("user端插入关注记录成功: userId={}, followId={}", userId, followId);
            } catch (DuplicateKeyException e) {
                log.warn("user端关注记录已存在(并发插入), 跳过: userId={}, followId={}", userId, followId);
            }
        } else {
            log.info("user端关注记录已存在，跳过: userId={}, followId={}", userId, followId);
        }
    }

    private void handleCancelFollow(Integer userId, Integer followId) {
        QueryWrapper<ApUserFollow> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("user_id", userId);
        deleteWrapper.eq("follow_id", followId);
        int deleted = apUserFollowMapper.delete(deleteWrapper);
        log.info("user端取消关注成功: userId={}, followId={}, 删除行数={}", userId, followId, deleted);
    }
}
