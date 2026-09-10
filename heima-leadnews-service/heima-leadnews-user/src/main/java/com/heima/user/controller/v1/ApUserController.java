package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.user.service.ApUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Api(tags = "APP端用户")
public class ApUserController {

    private final ApUserService apUserService;

    @ApiOperation("关注与取消关注")
    @PostMapping("/user_follow")
    public ResponseResult userFollow(@RequestBody UserRelationDto dto) {
        return apUserService.userFollow(dto);
    }

    @ApiOperation("根据ID查询用户")
    @GetMapping("/getById")
    public ResponseResult getById(@RequestParam("userId") Integer userId) {
        return ResponseResult.okResult(apUserService.getById(userId));
    }

    @ApiOperation("检查是否关注")
    @GetMapping("/checkFollow")
    public ResponseResult checkFollow(@RequestParam("userId") Integer userId,
                                      @RequestParam("followId") Long followId) {
        return apUserService.checkFollow(userId, followId);
    }
}
