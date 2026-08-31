package com.heima.article.feign;

import com.heima.apis.article.IArticleClient;
import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;




@RestController
public class ArticleClient implements IArticleClient {

    @Autowired
    private ApArticleService apArticleService;
    /**
     * 保存文章
     *
     * @param dto
     * @return
     */
    @Override
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto) {
        return apArticleService.saveArticle(dto);
    }

    @Override
    @GetMapping("/api/v1/article/channel/{id}/count")
    public ResponseResult getArticleCountByChannelId(@PathVariable("id") Integer id) {
        return apArticleService.getArticleCountByChannelId(id);
    }
}
