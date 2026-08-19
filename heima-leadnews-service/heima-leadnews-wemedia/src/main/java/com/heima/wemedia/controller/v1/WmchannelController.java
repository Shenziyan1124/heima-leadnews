package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.wemedia.service.WmChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/channel")
@RequiredArgsConstructor
@Api(tags = "频道管理")
public class WmchannelController {

    private final WmChannelService wmChannelService;

    @ApiOperation(value = "查询频道列表")
    @GetMapping("/channels")
    public ResponseResult findAllChannel() {
       return wmChannelService.findAllChannel();
    }
}
