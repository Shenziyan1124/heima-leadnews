package com.heima.article.feign;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.heima.apis.article.IArticleClient;
import com.heima.article.service.ApArticleConfigService;
import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    @GetMapping("/api/v1/article/newsDimension")
    public ResponseResult getNewsDimension(@RequestParam(value = "beginDate", required = false) String beginDate,
                                           @RequestParam(value = "endDate", required = false) String endDate,
                                           @RequestParam("id") Integer id) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        LambdaQueryChainWrapper<ApArticle> wrapper = apArticleService.lambdaQuery()
                .eq(ApArticle::getAuthorId, id);

        if (beginDate != null && !beginDate.isEmpty()) {
            wrapper.ge(ApArticle::getPublishTime, sdf.parse(beginDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(ApArticle::getPublishTime, sdf.parse(endDate));
        }

        // 统计发布量
        Long publishNum = Long.valueOf(wrapper.count());

        Map<String, Object> result = new HashMap<>();
        result.put("publishNum", publishNum.intValue());
        result.put("likesNum", 0);
        result.put("collectNum", 0);
        result.put("readNum", 0);
        result.put("commentNum", 0);

        return ResponseResult.okResult(result);
    }
}
