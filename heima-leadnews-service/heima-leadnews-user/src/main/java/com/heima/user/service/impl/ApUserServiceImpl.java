package com.heima.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.common.constants.RealNameStatusConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserFollow;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.user.mapper.ApRealNameMapper;
import com.heima.user.mapper.ApUserFollowMapper;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
@Transactional // 事务管理
@Service // 服务层
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {

    @Autowired
    private ApUserFollowMapper apUserFollowMapper;

    @Autowired
    private ApRealNameMapper apRealNameMapper;

    @Autowired
    private ApUserMapper apUserMapper;
    @Autowired
    private IWemediaClient wemediaClient;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 登录
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(LoginDto dto) {
        // 1. 正常用户登陆
        if (StringUtils.isNotBlank(dto.getPhone()) && StringUtils.isNotBlank(dto.getPassword())) {
            // 1.1 根据手机号去查询
            ApUser dbUser = lambdaQuery().eq(ApUser::getPhone, dto.getPhone()).one();
            if (dbUser == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "用户不存在");
            }
            // 1.2 对比密码
            String salt = dbUser.getSalt();
            String pwd = dto.getPassword(); // 用户输入的密码
            String md5Pwd = DigestUtils.md5DigestAsHex((pwd + salt).getBytes()); // 输入密码加密
            if (!md5Pwd.equals(dbUser.getPassword())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
            // 1.3 返回 jwt user
            String token = AppJwtUtil.getToken(Long.valueOf(dbUser.getId()));
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", token);
            dbUser.setSalt(null);
            dbUser.setPassword(null);
            map.put("user", dbUser);

            return ResponseResult.okResult(map);
        } else {
            // 游客登陆
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(0L));
            return ResponseResult.okResult(map);
        }
    }

    /**
     * 用户关注
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult userFollow(UserRelationDto dto) {

        // 1. 参数校验
        if (dto.getAuthorId() == null || dto.getOperation() == null ||
                dto.getOperation() < ApUserBehaviorConstants.FOLLOW ||
                dto.getOperation() > ApUserBehaviorConstants.CANCEL_FOLLOW ||
                dto.getAuthorId() < 0
        ) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 1.1 获取当前登录用户id
        //Integer userId = AppThreadLocalUtil.getUser().getId();
        Integer userId = 4;

        // 2. 取消关注
        if (dto.getOperation() == ApUserBehaviorConstants.CANCEL_FOLLOW) {
            // 2.1 删Redis
            cacheService.sRemove(ApUserBehaviorConstants.FOLLOW_USER_KEY + userId, String.valueOf(dto.getAuthorId()));
            cacheService.sRemove(ApUserBehaviorConstants.FOLLOW_FANS_KEY + dto.getAuthorId(), String.valueOf(userId));

            // 2.2 发Kafka
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("followId", dto.getAuthorId());
            map.put("operation", dto.getOperation());
            kafkaTemplate.send(ApUserBehaviorConstants.FOLLOW_KAFKA_TOPIC, JSON.toJSONString(map));
            log.info("取消关注 Kafka消息已发送: userId={}, followId={}", userId, dto.getAuthorId());

            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }

        // 3. 关注 - 校验博主
        // 3.1 查询是否是自媒体博主
        // 3.1.1 先查redis,有直接用,无再feign查询
        String redisKey = "wemedia:author:" + dto.getAuthorId();
        String cached = cacheService.get(redisKey);
        WmUser wmUser = null;
        if (cached != null) {
            wmUser = JSON.parseObject(cached, WmUser.class);
        } else {
            // Redis中没有,去feign查询
            for (int i = 0; i < 3; i++) {
                ResponseResult result = wemediaClient.getWmUserByUserId(dto.getAuthorId());
                if (result.getData() != null) {
                    wmUser = new ObjectMapper().convertValue(result.getData(), WmUser.class);
                    // 写入Redis
                    cacheService.setEx(redisKey, JSON.toJSONString(wmUser), 30, TimeUnit.MINUTES);
                    break;
                }
                log.warn("Feign第{}次调用wemedia失败，重试...", i + 1);
            }
        }
        // 3.1.2 判断自媒体博主
        if (wmUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "该用户不是自媒体博主");
        }

        // 3.2 查询作者是否在ap_user_realname (实名认证)
        QueryWrapper<ApUserRealname> realNameQueryWrapper = new QueryWrapper<ApUserRealname>()
                .eq("user_id", wmUser.getApUserId())
                .eq("status", RealNameStatusConstants.STATUS_SUCCESS); // 状态为9表示审核通过
        ApUserRealname apUserRealNameDB = apRealNameMapper.selectOne(realNameQueryWrapper);
        if (apUserRealNameDB == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "该作者未实名认证");
        }

        // 3.3 查询作者是否在ap_user中
        QueryWrapper<ApUser> userQueryWrapper = new QueryWrapper<ApUser>()
                .eq("id", wmUser.getApUserId());
        ApUser apUserDB = apUserMapper.selectOne(userQueryWrapper);
        if (apUserDB == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "关注的作者不存在");
        }

        // 3.4 查询用户是否已经关注过该作者 (先查Redis)
        boolean isMember = cacheService.sIsMember(ApUserBehaviorConstants.FOLLOW_USER_KEY + userId, String.valueOf(dto.getAuthorId()));
        if (isMember) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "已关注该作者");
        }

        // 3.5 再查MySQL确认
        QueryWrapper<ApUserFollow> followQueryWrapper = new QueryWrapper<ApUserFollow>()
                .eq("user_id", userId)
                .eq("follow_id", dto.getAuthorId());
        ApUserFollow apUserFollowDB = apUserFollowMapper.selectOne(followQueryWrapper);
        if (apUserFollowDB != null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "已关注该作者");
        }

        // 4. 写Redis
        cacheService.sAdd(ApUserBehaviorConstants.FOLLOW_USER_KEY + userId, String.valueOf(dto.getAuthorId()));
        cacheService.sAdd(ApUserBehaviorConstants.FOLLOW_FANS_KEY + dto.getAuthorId(), String.valueOf(userId));

        // 5. 发Kafka
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("followId", dto.getAuthorId());
        map.put("followName", apUserDB.getName());
        map.put("operation", dto.getOperation());
        kafkaTemplate.send(ApUserBehaviorConstants.FOLLOW_KAFKA_TOPIC, JSON.toJSONString(map));
        log.info("关注 Kafka消息已发送: userId={}, followId={}", userId, dto.getAuthorId());

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult checkFollow(Integer userId, Long followId) {
        Integer exists = apUserFollowMapper.selectCount(new QueryWrapper<ApUserFollow>()
                .eq("user_id", userId)
                .eq("follow_id", followId)
        );
        return ResponseResult.okResult(exists);
    }
}
