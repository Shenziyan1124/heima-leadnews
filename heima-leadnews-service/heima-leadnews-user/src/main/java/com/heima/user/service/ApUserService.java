package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.model.user.pojos.ApUser;

public interface ApUserService extends IService<ApUser> {
    /**
     * 登录
     * @param dto
     * @return
     */
    ResponseResult login(LoginDto dto);

    /**
     * 用户关注
     * @param dto
     * @return
     */
    ResponseResult userFollow(UserRelationDto dto);
}
