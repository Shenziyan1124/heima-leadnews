package com.heima.article.feign;

import com.heima.apis.article.IArticleClient;
import com.heima.article.service.ApArticleConfigService;
import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ArticleClient implements IArticleClient {

    @Autowired
    private ApArticleService apArticleService;
    @Autowired
    private ApArticleConfigService apArticleConfigService;

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

    @Override
    @GetMapping("/api/v1/article/comment_status/{articleId}")
    public ResponseResult getCommentStatus(@PathVariable("articleId") Long articleId) {
        ApArticleConfig config = apArticleConfigService.lambdaQuery()
                .eq(ApArticleConfig::getArticleId, articleId)
                .one();
        if (config != null) {
            return ResponseResult.okResult(config.getIsComment());
        }
        return ResponseResult.okResult(true);
    }

    @Override
    @PostMapping("/api/v1/article/comment_status")
    public ResponseResult updateCommentStatus(@RequestParam("articleId") Long articleId, @RequestParam("isComment") Boolean isComment) {
        apArticleConfigService.lambdaUpdate()
                .eq(ApArticleConfig::getArticleId, articleId)
                .set(ApArticleConfig::getIsComment, isComment)
                .update();
        return ResponseResult.okResult(null);
    }
}
