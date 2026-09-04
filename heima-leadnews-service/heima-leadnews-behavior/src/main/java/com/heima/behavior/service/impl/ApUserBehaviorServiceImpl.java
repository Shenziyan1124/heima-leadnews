package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.behavior.service.ApUserBehaviorService;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.behavior.dtos.CollectionBehaviorDto;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ApUserBehaviorServiceImpl implements ApUserBehaviorService {


    private final CacheService cacheService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 用户点赞行为
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveLikesBehavior(LikesBehaviorDto dto) {

        // 1.校验参数
        if (dto == null ||
                dto.getArticleId() == null ||
                dto.getType() == null || dto.getType() < 0 || dto.getType() > 2 ||
                dto.getOperation() == null || dto.getOperation() < 0 ||  dto.getOperation() > 1
        ) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2.校验用户是否登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Integer userId = user.getId();
        //Integer userId = 4;

        // 3.判断是点赞还是取消点赞
        if (Objects.equals(dto.getOperation(), ApUserBehaviorConstants.LIKE)){
            // 点赞
            // 1.写redis中
            cacheService.sAdd(ApUserBehaviorConstants.LIKES_ARTICLE_KEY  + dto.getArticleId(),userId.toString());
            cacheService.sAdd(ApUserBehaviorConstants.LIKES_USER_KEY  + userId, dto.getArticleId().toString());

            // 2.发送kafka,异步同步mysql
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("articleId", dto.getArticleId());
            map.put("type", dto.getType());
            map.put("operation", dto.getOperation());
            kafkaTemplate.send(ApUserBehaviorConstants.LIKES_KAFKA_TOPIC, JSON.toJSONString(map));

        }else {
            // 取消点赞
            // 1.删除redis
            cacheService.sRemove(ApUserBehaviorConstants.LIKES_ARTICLE_KEY  + dto.getArticleId(), userId.toString());
            cacheService.sRemove(ApUserBehaviorConstants.LIKES_USER_KEY + userId, dto.getArticleId().toString());

            // 2.发送kafka,删除mysql
            Map<String, Object> map2 = new HashMap<>();
            map2.put("userId", userId);
            map2.put("articleId", dto.getArticleId());
            map2.put("type", dto.getType());
            map2.put("operation", dto.getOperation());
            kafkaTemplate.send(ApUserBehaviorConstants.LIKES_KAFKA_TOPIC, JSON.toJSONString(map2));
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

        //Boolean isLike = cacheService.sIsMember(
        //        ApUserBehaviorConstants.LIKES_ARTICLE_KEY + articleId, userId.toString(
        //        ));
    }

    /**
     * 用户阅读行为
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveReadBehavior(ReadBehaviorDto dto) {
        log.info("用户阅读行为: {}", dto);
        if (dto == null || dto.getArticleId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 1.获取用户ID
        Integer userId = AppThreadLocalUtil.getUser().getId();
        //Integer userId = 4;

        // 2.更新Redis阅读次数
        String key = ApUserBehaviorConstants.READ_COUNT_KEY + dto.getArticleId();
        cacheService.incrBy(key, 1);

        // 3.发送Kafka，article服务更新MySQL
        Map<String, Object> map = new HashMap<>();
        map.put("articleId", dto.getArticleId());
        map.put("userId", userId);
        map.put("equipmentId", dto.getEquipmentId() == null ? null : dto.getEquipmentId());
        map.put("readDuration", dto.getReadDuration() == null ? null : dto.getReadDuration());
        map.put("percentage", dto.getPercentage() == null ? null : dto.getPercentage());
        kafkaTemplate.send(ApUserBehaviorConstants.READ_KAFKA_TOPIC, JSON.toJSONString(map));

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 用户不喜欢行为
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveUnLikeBehavior(UnLikesBehaviorDto dto) {
        // 1.校验参数
        if (dto == null ||
                dto.getArticleId() == null ||
                dto.getType() == null || dto.getType() < 0 || dto.getType() > 2
        ) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2.校验用户是否登录
        Integer userId = AppThreadLocalUtil.getUser().getId();
        if (userId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        //Integer userId = 4;

        // 3.写入Redis
        // 映射type值：前端0→数据库2，前端1→数据库3
        Short dbOperation = (short) (dto.getType() + 2);
        if (dbOperation == ApUserBehaviorConstants.UN_LIKE){
            // 不喜欢
            cacheService.sAdd(ApUserBehaviorConstants.UN_LIKE_ARTICLE_KEY + dto.getArticleId(), userId.toString());
            cacheService.sAdd(ApUserBehaviorConstants.UN_LIKE_USER_KEY + userId, dto.getArticleId().toString());

            // 发送Kafka，article服务更新MySQL
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("articleId", dto.getArticleId());
            map.put("type", ApUserBehaviorConstants.LIKES_ARTICLE_TYPE);
            map.put("operation", dbOperation); // 0 只有文章才有不喜欢
            kafkaTemplate.send(ApUserBehaviorConstants.UN_LIKE_KAFKA_TOPIC, JSON.toJSONString(map));
        }else {
            // 取消不喜欢
            cacheService.sRemove(ApUserBehaviorConstants.UN_LIKE_ARTICLE_KEY + dto.getArticleId(), userId.toString());
            cacheService.sRemove(ApUserBehaviorConstants.UN_LIKE_USER_KEY + userId, dto.getArticleId().toString());

            // 发送Kafka，article服务更新MySQL
            Map<String, Object> map2 = new HashMap<>();
            map2.put("userId", userId);
            map2.put("articleId", dto.getArticleId());
            map2.put("type", ApUserBehaviorConstants.LIKES_ARTICLE_TYPE);  // 0 只有文章才有不喜欢
            map2.put("operation", dbOperation);
            kafkaTemplate.send(ApUserBehaviorConstants.UN_LIKE_KAFKA_TOPIC, JSON.toJSONString(map2));
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 用户收藏行为
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveCollectionBehavior(CollectionBehaviorDto dto) {
        // 1.校验参数
        if (dto == null || dto.getEntryId() == null ||
                dto.getOperation() == null || dto.getOperation() < 0 || dto.getOperation() > 1 ||
                dto.getType() == null || dto.getType() < 0 || dto.getType() > 1
        ) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2.校验用户是否登录
        Integer userId = AppThreadLocalUtil.getUser().getId();
        if (userId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        //Integer userId = 4;

        // 3.写入Redis
        if (Objects.equals(dto.getOperation(), ApUserBehaviorConstants.COLLECT)){
            // 收藏
            cacheService.sAdd(ApUserBehaviorConstants.COLLECT_ARTICLE_KEY + dto.getEntryId(), userId.toString());
            cacheService.sAdd(ApUserBehaviorConstants.COLLECT_USER_KEY + userId, dto.getEntryId().toString());

            // 发送Kafka，article服务更新MySQL
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("articleId", dto.getEntryId());
            map.put("type", dto.getType());
            map.put("operation", dto.getOperation());
            kafkaTemplate.send(ApUserBehaviorConstants.COLLECT_KAFKA_TOPIC, JSON.toJSONString(map));
        }else {
            // 取消收藏
            cacheService.sRemove(ApUserBehaviorConstants.COLLECT_ARTICLE_KEY + dto.getEntryId(), userId.toString());
            cacheService.sRemove(ApUserBehaviorConstants.COLLECT_USER_KEY + userId, dto.getEntryId().toString());

            // 发送Kafka，article服务更新MySQL
            Map<String, Object> map2 = new HashMap<>();
            map2.put("userId", userId);
            map2.put("articleId", dto.getEntryId());
            map2.put("type", dto.getType());
            map2.put("operation", dto.getOperation());
            kafkaTemplate.send(ApUserBehaviorConstants.COLLECT_KAFKA_TOPIC, JSON.toJSONString(map2));
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
