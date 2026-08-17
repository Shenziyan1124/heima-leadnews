package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/article")
@RequiredArgsConstructor
@Api(tags = "文章")
public class ArticleHomeController {

    private final ApArticleService apArticleService;
    /**
     * 加载首页
     * */
    @PostMapping("/load")
    @ApiOperation(value = "加载文章列表")
    public ResponseResult load(@RequestBody ArticleHomeDto dto){
        return apArticleService.loadArticleList(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    /**
     * 加载更多
     * */
    @PostMapping("/loadmore")
    @ApiOperation(value = "加载更多文章列表")
    public ResponseResult loadMore(@RequestBody ArticleHomeDto dto){
        return apArticleService.loadArticleList(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    /**
     * 加载最新
     * */
    @ApiOperation(value = "加载最新文章列表")
    @PostMapping("/loadnew")
    public ResponseResult loadNew(@RequestBody ArticleHomeDto dto){
        return apArticleService.loadArticleList(dto, ArticleConstants.LOADTYPE_LOAD_NEW);
    }
}
