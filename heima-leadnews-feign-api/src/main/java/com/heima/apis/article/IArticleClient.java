package com.heima.apis.article;

import com.heima.apis.article.fallback.IArticleClientFallback;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "leadnews-article",fallbackFactory = IArticleClientFallback.class)
public interface IArticleClient {

    /**
     * 保存文章
     * @param dto
     * @return
     */

    @PostMapping("/api/v1/article/save")
    ResponseResult saveArticle(@RequestBody ArticleDto dto);


    /**
     * 根据频道id获取文章数量
     * @param id
     * @return
     */
    @GetMapping("/api/v1/article/channel/{id}/count")
    ResponseResult getArticleCountByChannelId(@PathVariable("id") Integer id);
}
