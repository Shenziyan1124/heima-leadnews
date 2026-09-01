package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmLoginDto;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.AppJwtUtil;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class WmUserServiceImpl extends ServiceImpl<WmUserMapper, WmUser> implements WmUserService {

    @Override
    public ResponseResult login(WmLoginDto dto) {
        //1.检查参数
        if(StringUtils.isBlank(dto.getName()) || StringUtils.isBlank(dto.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"用户名或密码为空");
        }

        //2.查询用户
        WmUser wmUser = getOne(Wrappers.<WmUser>lambdaQuery().eq(WmUser::getName, dto.getName()));
        if(wmUser == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        //3.比对密码
        String salt = wmUser.getSalt();
        String pswd = dto.getPassword();
        pswd = DigestUtils.md5DigestAsHex((pswd + salt).getBytes());
        if(pswd.equals(wmUser.getPassword())){
            //4.返回数据  jwt
            Map<String,Object> map  = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(wmUser.getId().longValue()));
            wmUser.setSalt("");
            wmUser.setPassword("");
            map.put("user",wmUser);
            return ResponseResult.okResult(map);

        }else {
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }
    }

    /**
     * 创建自媒体用户
     *
     * @param wmUser
     * @return
     */
    @Override
    public ResponseResult createWmUser(WmUser wmUser) {
        log.info("接收Feign请求: wmUser={}", wmUser);

        // 1.检查用户是否存在
        LambdaQueryWrapper<WmUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WmUser::getName, wmUser.getName());
        int count = count(queryWrapper);
        log.info("查询用户数量: count={}", count);

        if (count > 0){
            log.info("用户已存在，返回DATA_EXIST");
            ResponseResult result = ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "用户已存在");
            log.info("返回结果: {}", result);
            return result;
        }

        // 2.保存用户
        WmUser user = new WmUser();
        BeanUtils.copyProperties(wmUser, user);
        log.info("保存用户: user={}", user);
        save(user);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}