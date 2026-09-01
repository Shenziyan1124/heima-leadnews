package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmAdminNewsReqDto;
import com.heima.model.wemedia.dtos.WmNewsDownOrUpDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.WmNewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
@Api(tags = "新闻管理")
@RequiredArgsConstructor
public class WmNewsController {
    private final WmNewsService wmNewsService;


    @PostMapping("/list")
    @ApiOperation("查询文章列表")
    public ResponseResult findAll(@RequestBody WmNewsPageReqDto dto){
        return wmNewsService.findAll(dto);
    }

    @PostMapping("/submit")
    @ApiOperation("发布/修改/保存草稿文章")
    public ResponseResult submitNews(@RequestBody WmNewsDto dto){
        return wmNewsService.submitNews(dto);
    }

    @GetMapping("/one/{id}")
    @ApiOperation("获取文章信息")
    public ResponseResult getNewsDetail(@PathVariable("id") Integer id){
        return wmNewsService.getNewsDetail(id);
    }

    @GetMapping("/del_news/{id}")
    @ApiOperation("删除文章")
    public ResponseResult delNews(@PathVariable("id") Integer id){
        return wmNewsService.delNews(id);
    }

    @PostMapping("/down_or_up")
    @ApiOperation("文章上下架")
    public ResponseResult downOrUp(@RequestBody WmNewsDownOrUpDto dto){
        return wmNewsService.downOrUp(dto);
    }



    @PostMapping("/list_vo")
    @ApiOperation("查询admin端文章列表")
    public ResponseResult findAdminList(@RequestBody WmAdminNewsReqDto dto){
        return wmNewsService.findAdminList(dto);
    }

    @GetMapping("/one_vo/{id}")
    @ApiOperation("获取admin端文章详情")
    public ResponseResult getAdminNewsDetail(@PathVariable("id") @Required Integer id){
        return wmNewsService.getAdminNewsDetail(id);
    }

    @PostMapping("/auth_fail")
    @ApiOperation("admin文章审核失败")
    public ResponseResult adminAuthFail(@RequestBody WmAdminNewsReqDto dto){
        return wmNewsService.adminAuthFail(dto);
    }

    @PostMapping("/auth_pass")
    @ApiOperation("admin文章审核通过")
    public ResponseResult adminAuthPass(@RequestBody WmAdminNewsReqDto dto){
        return wmNewsService.adminAuthPass(dto);
    }


}
