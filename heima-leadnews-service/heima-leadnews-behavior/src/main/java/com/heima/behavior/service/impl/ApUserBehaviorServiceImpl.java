package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.behavior.service.ApUserBehaviorService;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
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
        //ApUser user = AppThreadLocalUtil.getUser();
        //if (user == null) {
        //    return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        //}
        Integer userId = 4;

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

        //Integer userId = AppThreadLocalUtil.getUser().getId();
        String key = ApUserBehaviorConstants.READ_COUNT_KEY + dto.getArticleId();
        cacheService.incrBy(key, 1);

        kafkaTemplate.send(ApUserBehaviorConstants.READ_KAFKA_TOPIC, String.valueOf(dto.getArticleId()));

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
