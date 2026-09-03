package com.heima.article.listener;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.mapper.ApLikesBehaviorMapper;
import com.heima.article.mapper.ApReadHistoryMapper;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApLikesBehavior;
import com.heima.model.article.pojos.ApReadHistory;
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
public class BehaviorListener {

    @Autowired
    private ApLikesBehaviorMapper apLikesBehaviorMapper;
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private ApReadHistoryMapper apReadHistoryMapper;

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
                    // 点赞 - 先检查是否已存在
                    LambdaQueryWrapper<ApLikesBehavior> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(ApLikesBehavior::getArticleId, articleId);
                    queryWrapper.eq(ApLikesBehavior::getUserId, userId);
                    queryWrapper.eq(ApLikesBehavior::getType, type);
                    ApLikesBehavior existRecord = apLikesBehaviorMapper.selectOne(queryWrapper);

                    if (existRecord == null) {
                        // 不存在，插入新记录
                        try {
                            ApLikesBehavior apLikesBehavior = new ApLikesBehavior();
                            apLikesBehavior.setUserId(userId);
                            apLikesBehavior.setArticleId(articleId);
                            apLikesBehavior.setType(type);
                            apLikesBehavior.setCreatedTime(new Date());
                            apLikesBehavior.setUpdateTime(new Date());
                            apLikesBehavior.setIsDelete(ApUserBehaviorConstants.CANCEL_DELETE);
                            apLikesBehaviorMapper.insert(apLikesBehavior);
                            log.info("article端插入点赞记录成功: {}", apLikesBehavior);
                        } catch (DuplicateKeyException e) {
                            log.warn("article端点赞记录已存在(并发插入), 跳过: articleId={}, userId={}", articleId, userId);
                        }
                    } else if (existRecord.getIsDelete() == ApUserBehaviorConstants.DELETE) {
                        // 已存在但被删除过，恢复点赞
                        LambdaUpdateWrapper<ApLikesBehavior> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.eq(ApLikesBehavior::getId, existRecord.getId());
                        updateWrapper.set(ApLikesBehavior::getIsDelete, ApUserBehaviorConstants.CANCEL_DELETE);
                        updateWrapper.set(ApLikesBehavior::getUpdateTime, new Date());
                        apLikesBehaviorMapper.update(null, updateWrapper);
                        log.info("article端恢复点赞记录成功: articleId={}, userId={}", articleId, userId);
                    } else {
                        log.info("article端点赞记录已存在，跳过: articleId={}, userId={}", articleId, userId);
                    }

                    // 更新文章点赞数
                    LambdaUpdateWrapper<ApArticle> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(ApArticle::getId, articleId);
                    updateWrapper.setSql("likes = likes + 1");
                    apArticleMapper.update(null, updateWrapper);
                } else {

                    // 点赞 - 先检查是否已存在
                    LambdaQueryWrapper<ApLikesBehavior> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(ApLikesBehavior::getArticleId, articleId);
                    queryWrapper.eq(ApLikesBehavior::getUserId, userId);
                    queryWrapper.eq(ApLikesBehavior::getType, type);
                    ApLikesBehavior existRecord = apLikesBehaviorMapper.selectOne(queryWrapper);

                    if (existRecord == null) {
                        log.info("article端取消点赞记录不存在, 跳过: articleId={}, userId={}", articleId, userId);
                        return;
                    }

                    if (existRecord.getIsDelete() == ApUserBehaviorConstants.DELETE){
                        log.info("article端取消点赞记录已删除, 跳过: articleId={}, userId={}", articleId, userId);
                        return;
                    }

                    // 取消点赞 - 软删除（更新is_delete=1）
                    LambdaUpdateWrapper<ApLikesBehavior> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(ApLikesBehavior::getArticleId, articleId);
                    updateWrapper.eq(ApLikesBehavior::getUserId, userId);
                    updateWrapper.eq(ApLikesBehavior::getType, type);
                    updateWrapper.set(ApLikesBehavior::getIsDelete, ApUserBehaviorConstants.DELETE);
                    updateWrapper.set(ApLikesBehavior::getUpdateTime, new Date());
                    apLikesBehaviorMapper.update(null, updateWrapper);

                    LambdaUpdateWrapper<ApArticle> updateWrapper2 = new LambdaUpdateWrapper<>();
                    updateWrapper2.eq(ApArticle::getId, articleId);
                    updateWrapper2.setSql("likes = likes - 1");
                    apArticleMapper.update(null, updateWrapper2);

                    log.info("article端软删除用户点赞行为数据成功: articleId={}, userId={}", articleId, userId);
                }
            }
        } catch (Exception e) {
            log.error("Kafka消费点赞消息失败，等待重试: {}", e.getMessage());
            throw e; // 抛出异常，Kafka会自动重试
        }
    }



    @KafkaListener(topics = ApUserBehaviorConstants.READ_KAFKA_TOPIC)
    public void readBehaviorListener(String message) {
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
                    // 首次阅读，插入记录
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
                    // 已读过，更新次数和时间
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
            throw e; // 抛出异常，Kafka会自动重试
        }
    }



    @KafkaListener(topics = ApUserBehaviorConstants.UN_LIKE_KAFKA_TOPIC)
    public void unLikeBehaviorListener(String message) {
        log.info("article端收到unLike Kafka的消息: {}", message);
        try {
            if (StringUtils.isNotBlank(message)) {
                Map map = JSON.parseObject(message, Map.class);
                Integer userId = ((Number) map.get("userId")).intValue();
                Long articleId = ((Number) map.get("articleId")).longValue();
                Short type = ((Number) map.get("type")).shortValue();

                if (Objects.equals(type, ApUserBehaviorConstants.UN_LIKE)) {
                    // 不喜欢(type=2) - 插入记录
                    LambdaQueryWrapper<ApLikesBehavior> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(ApLikesBehavior::getArticleId, articleId);
                    queryWrapper.eq(ApLikesBehavior::getUserId, userId);
                    queryWrapper.eq(ApLikesBehavior::getType, type);
                    ApLikesBehavior existRecord = apLikesBehaviorMapper.selectOne(queryWrapper);

                    if (existRecord == null) {
                        ApLikesBehavior record = new ApLikesBehavior();
                        record.setArticleId(articleId);
                        record.setUserId(userId);
                        record.setType(type);
                        record.setCreatedTime(new Date());
                        record.setUpdateTime(new Date());
                        record.setIsDelete(ApUserBehaviorConstants.CANCEL_DELETE);
                        apLikesBehaviorMapper.insert(record);
                        log.info("article端插入不喜欢记录成功: articleId={}, userId={}", articleId, userId);
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

                } else {
                    // 取消不喜欢(type=3) - 软删除type=2的记录
                    LambdaUpdateWrapper<ApLikesBehavior> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(ApLikesBehavior::getArticleId, articleId);
                    updateWrapper.eq(ApLikesBehavior::getUserId, userId);
                    updateWrapper.eq(ApLikesBehavior::getType, ApUserBehaviorConstants.UN_LIKE);
                    updateWrapper.set(ApLikesBehavior::getIsDelete, ApUserBehaviorConstants.DELETE);
                    updateWrapper.set(ApLikesBehavior::getUpdateTime, new Date());
                    apLikesBehaviorMapper.update(null, updateWrapper);
                    log.info("article端软删除不喜欢记录成功: articleId={}, userId={}", articleId, userId);
                }
            }
        } catch (Exception e) {
            log.error("Kafka消费不喜欢消息失败，等待重试: {}", e.getMessage());
            throw e;
        }
    }
}
