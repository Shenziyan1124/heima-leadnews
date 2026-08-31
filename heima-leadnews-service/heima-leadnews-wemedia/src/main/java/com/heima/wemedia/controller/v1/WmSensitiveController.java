package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelListDto;
import com.heima.model.wemedia.dtos.WmSensitiveDto;
import com.heima.wemedia.service.WmSensitiveService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sensitive")
@Api(tags = "敏感信息管理")
@RequiredArgsConstructor
public class WmSensitiveController {

    private final WmSensitiveService wmSensitiveService;

    //@DeleteMapping("/del/{id}")
    //public ResponseResult delete(@PathVariable Long id) {
    //    return ResponseResult;
    //}

    @PostMapping("/list")
    @ApiOperation(value = "敏感信息列表", notes = "分页查询敏感信息列表")
    public ResponseResult getSensitiveList(@RequestBody WmChannelListDto dto) {
        return wmSensitiveService.getSensitiveList(dto);
    }

    @PostMapping("/save")
    @ApiOperation(value = "保存敏感信息", notes = "保存敏感信息")
    public ResponseResult save(@RequestBody WmSensitiveDto dto) {
        return wmSensitiveService.saveOrUpdateSensitive(dto);
    }

    @PostMapping("/update")
    @ApiOperation(value = "编辑敏感信息", notes = "编辑敏感信息")
    public ResponseResult update(@RequestBody WmSensitiveDto dto) {
        return wmSensitiveService.saveOrUpdateSensitive(dto);
    }

    @DeleteMapping("/del/{id}")
    @ApiOperation(value = "删除敏感信息", notes = "删除敏感信息")
    public ResponseResult delete(@PathVariable Long id) {
        return wmSensitiveService.deleteById(id);
    }
}
