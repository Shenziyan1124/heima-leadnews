package com.heima.behavior.controller.v1;

import com.heima.behavior.service.ApUserBehaviorService;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/behavior")
@Api(tags = "用户行为接口")
@RequiredArgsConstructor
public class ApUserBehavior {

    private final ApUserBehaviorService apUserBehaviorService;

    @PostMapping("/likes_behavior")
    @ApiOperation("用户点赞行为")
    public ResponseResult saveLikesBehavior(@RequestBody LikesBehaviorDto dto) {
        return apUserBehaviorService.saveLikesBehavior(dto);
    }


    @PostMapping("/read_behavior")
    @ApiOperation("用户阅读行为")
    public ResponseResult saveReadBehavior(@RequestBody ReadBehaviorDto dto) {
        return apUserBehaviorService.saveReadBehavior(dto);
    }

}
