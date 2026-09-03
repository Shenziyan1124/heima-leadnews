package com.heima.article.listener;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.mapper.ApReadHistoryMapper;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApReadHistory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class ReadBehaviorListener {

    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private ApReadHistoryMapper apReadHistoryMapper;

    @KafkaListener(topics = ApUserBehaviorConstants.READ_KAFKA_TOPIC)
    public void onMessage(String message) {
        log.info("article端收到read Kafka的消息: {}", message);

        try {
            if (StringUtils.isNotBlank(message)) {
                Map map = JSON.parseObject(message, Map.class);

                Long articleId = ((Number) map.get("articleId")).longValue();
                Integer userId = ((Number) map.get("userId")).intValue();
                Integer equipmentId = map.get("equipmentId") != null ? ((Number) map.get("equipmentId")).intValue() : null;
                Integer readDuration = map.get("readDuration") != null ? ((Number) map.get("readDuration")).intValue() : null;
                Integer percentage = map.get("percentage") != null ? ((Number) map.get("percentage")).intValue() : null;

                // 1. 更新文章总阅读量
                LambdaUpdateWrapper<ApArticle> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(ApArticle::getId, articleId);
                updateWrapper.setSql("views = views + 1");
                apArticleMapper.update(null, updateWrapper);

                // 2. 插入/更新阅读历史
                LambdaQueryWrapper<ApReadHistory> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ApReadHistory::getArticleId, articleId);
                queryWrapper.eq(ApReadHistory::getUserId, userId);

                ApReadHistory existHistory = apReadHistoryMapper.selectOne(queryWrapper);

                if (existHistory == null) {
                    ApReadHistory history = new ApReadHistory();
                    history.setArticleId(articleId);
                    history.setUserId(userId);
                    history.setReadCount(1);
                    history.setEquipmentId(equipmentId);
                    history.setReadDuration(readDuration);
                    history.setPercentage(percentage);
                    history.setFirstReadTime(new Date());
                    history.setLastReadTime(new Date());
                    history.setCreatedTime(new Date());
                    history.setUpdateTime(new Date());
                    apReadHistoryMapper.insert(history);
                    log.info("article端插入阅读历史成功: articleId={}, userId={}", articleId, userId);
                } else {
                    existHistory.setReadCount(existHistory.getReadCount() + 1);
                    existHistory.setEquipmentId(equipmentId);
                    existHistory.setReadDuration(readDuration);
                    existHistory.setPercentage(percentage);
                    existHistory.setLastReadTime(new Date());
                    existHistory.setUpdateTime(new Date());
                    apReadHistoryMapper.updateById(existHistory);
                    log.info("article端更新阅读历史成功: articleId={}, userId={}, count={}", articleId, userId, existHistory.getReadCount());
                }
            }

        } catch (Exception e) {
            log.error("Kafka消费阅读消息失败，等待重试: {}", e.getMessage());
            throw e;
        }
    }
}
