package com.heima.admin.controller.v1;

import com.heima.admin.service.AdUserService;
import com.heima.model.admin.dtos.AdLoginDto;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@Api(tags = "admin登录接口")
@RequiredArgsConstructor
public class LoginController {

    private final AdUserService adUserService;

    @PostMapping("/in")
    @ApiOperation(value = "登录接口")
    public ResponseResult login(@RequestBody AdLoginDto dto){
        return adUserService.login(dto);
    }
}
