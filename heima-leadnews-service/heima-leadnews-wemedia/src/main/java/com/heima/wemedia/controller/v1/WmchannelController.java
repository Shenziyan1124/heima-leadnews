package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelDto;
import com.heima.model.wemedia.dtos.WmChannelslistDto;
import com.heima.wemedia.service.WmChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation(value = "频道名称模糊分页查询")
    @PostMapping("/list")
    public ResponseResult filterList(@RequestBody WmChannelslistDto dto) {
        return wmChannelService.filterList(dto);
    }


    @ApiOperation(value = "保存频道")
    @PostMapping("/save")
    public ResponseResult saveChannel(@RequestBody WmChannelDto dto) {
        return wmChannelService.saveOrUpdateChannel(dto);
    }

    @ApiOperation(value = "修改频道")
    @PostMapping("/update")
    public ResponseResult updateChannel(@RequestBody WmChannelDto dto) {
        return wmChannelService.saveOrUpdateChannel(dto);
    }

    @ApiOperation(value = "删除频道")
    @GetMapping("/del/{id}")
    public ResponseResult deleteChannel(@PathVariable("id") Integer id) {
        return wmChannelService.deleteChannel(id);
    }

}
