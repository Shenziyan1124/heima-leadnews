package com.heima.article.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.mapper.ApCollectionMapper;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApCollection;
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
public class CollectionBehaviorListener {

    @Autowired
    private ApCollectionMapper apCollectionMapper;
    @Autowired
    private ApArticleMapper apArticleMapper;


    @KafkaListener(topics = ApUserBehaviorConstants.COLLECT_KAFKA_TOPIC)
    public void onMessage(String message) {
        log.info("article端收到collect Kafka的消息: {}", message);

        try {
            if (StringUtils.isNotBlank(message)) {
                Map map = com.alibaba.fastjson.JSON.parseObject(message, Map.class);

                Integer userId = ((Number) map.get("userId")).intValue();
                Long articleId = ((Number) map.get("articleId")).longValue();
                Short type = ((Number) map.get("type")).shortValue();
                Short operation = ((Number) map.get("operation")).shortValue();

                if (Objects.equals(operation, ApUserBehaviorConstants.COLLECT)) {
                    handleCollect(userId, articleId, type);
                } else {
                    handleCancelCollect(userId, articleId, type);
                }
            }
        } catch (Exception e) {
            log.error("Kafka消费收藏消息失败，等待重试: {}", e.getMessage());
            throw e;
        }
    }

    private void handleCollect(Integer userId, Long articleId, Short type) {
        LambdaQueryWrapper<ApCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApCollection::getArticleId, articleId);
        queryWrapper.eq(ApCollection::getUserId, userId);
        queryWrapper.eq(ApCollection::getType, type);
        queryWrapper.eq(ApCollection::getIsDelete, (short) 0);
        ApCollection existRecord = apCollectionMapper.selectOne(queryWrapper);

        if (existRecord == null) {
            try {
                ApCollection record = new ApCollection();
                record.setArticleId(articleId);
                record.setUserId(userId);
                record.setType(type);
                record.setIsDelete((short) 0);
                record.setCollectionTime(new Date());
                record.setPublishedTime(new Date());
                apCollectionMapper.insert(record);



                log.info("article端插入收藏记录成功: articleId={}, userId={}", articleId, userId);
            } catch (DuplicateKeyException e) {
                log.warn("article端收藏记录已存在(并发插入), 跳过: articleId={}, userId={}", articleId, userId);
            }
        } else {
            log.info("article端收藏记录已存在，跳过: articleId={}, userId={}", articleId, userId);
        }

        LambdaUpdateWrapper<ApArticle> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ApArticle::getId, articleId);
        updateWrapper.setSql("collection = collection + 1");
        apArticleMapper.update(null, updateWrapper);
    }

    private void handleCancelCollect(Integer userId, Long articleId, Short type) {
        LambdaQueryWrapper<ApCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApCollection::getArticleId, articleId);
        queryWrapper.eq(ApCollection::getUserId, userId);
        queryWrapper.eq(ApCollection::getType, type);
        queryWrapper.eq(ApCollection::getIsDelete, (short) 0);
        ApCollection existRecord = apCollectionMapper.selectOne(queryWrapper);

        if (existRecord == null) {
            log.info("article端收藏记录不存在，跳过: articleId={}, userId={}", articleId, userId);
            return;
        }

        LambdaUpdateWrapper<ApCollection> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ApCollection::getId, existRecord.getId());
        updateWrapper.set(ApCollection::getIsDelete, (short) 1);
        updateWrapper.set(ApCollection::getPublishedTime, new Date());
        apCollectionMapper.update(null, updateWrapper);


        LambdaUpdateWrapper<ApArticle> updateWrapper2 = new LambdaUpdateWrapper<>();
        updateWrapper2.eq(ApArticle::getId, articleId);
        updateWrapper2.setSql("collection = GREATEST(IFNULL(collection, 0) - 1, 0)");
        apArticleMapper.update(null, updateWrapper2);

        log.info("article端取消收藏成功: articleId={}, userId={}", articleId, userId);
    }
}
