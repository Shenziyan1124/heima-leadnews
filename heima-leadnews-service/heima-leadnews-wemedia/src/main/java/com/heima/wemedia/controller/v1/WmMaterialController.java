package com.heima.wemedia.controller.v1;


import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialPageDto;
import com.heima.wemedia.service.WmMaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/material")
@Api(tags = "素材管理")
@RequiredArgsConstructor
public class WmMaterialController {

    private final WmMaterialService wmMaterialService;


    @PostMapping("/upload_picture")
    @ApiOperation(value = "上传图片", notes = "上传图片")
    public ResponseResult uploadPicture(MultipartFile multipartFile) throws IOException {
        return wmMaterialService.uploadPicture(multipartFile);
    }


    @PostMapping("/list")
    @ApiOperation(value = "素材列表", notes = "素材列表")
    public ResponseResult listMaterial(@RequestBody WmMaterialPageDto dto) {
        return wmMaterialService.listMaterial(dto);
    }

    @GetMapping("/del_picture/{id}")
    @ApiOperation(value = "删除图片", notes = "删除图片")
    public ResponseResult delPicture(@PathVariable("id") Integer id) {
       return wmMaterialService.delPicture(id);
    }

    @GetMapping("/cancel_collect/{id}")
    @ApiOperation(value = "取消收藏", notes = "取消收藏")
    public ResponseResult cancelCollect(@PathVariable("id") Integer id) {
        return wmMaterialService.cancelCollect(id);
    }

    @GetMapping("/collect/{id}")
    @ApiOperation(value = "收藏图片", notes = "收藏图片")
    public ResponseResult collect(@PathVariable("id") Integer id) {
        return wmMaterialService.collect(id);
    }
}
