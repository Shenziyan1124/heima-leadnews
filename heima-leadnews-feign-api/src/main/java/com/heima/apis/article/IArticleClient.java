package com.heima.apis.article;

import com.heima.apis.article.fallback.IArticleClientFallback;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmArticleListDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

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

    /**
     * 查询文章评论状态
     * @param articleId
     * @return
     */
    @GetMapping("/api/v1/article/comment_status/{articleId}")
    ResponseResult getCommentStatus(@PathVariable("articleId") Long articleId);

    /**
     * 更新文章评论状态
     * @param articleId
     * @param isComment
     * @return
     */
    @PostMapping("/api/v1/article/comment_status")
    ResponseResult updateCommentStatus(@RequestParam("articleId") Long articleId, @RequestParam("isComment") Boolean isComment);

    /**
     * 获取文章维度数据
     * @param beginDate
     * @param endDate
     * @param id
     * @return
     * @throws ParseException
     */
    @GetMapping("/api/v1/article/newsDimension")
    ResponseResult getNewsDimension(@RequestParam(value = "beginDate", required = false) String beginDate,
                                    @RequestParam(value = "endDate", required = false) String endDate,
                                    @RequestParam("id") Integer id) throws ParseException;


    /**
     * 获取作者文章分页列表
     * @param dto
     * @param id
     * @return
     */
    @PostMapping("/api/v1/article/authorNewsPage")
    ResponseResult getAuthorNewsPage(@RequestBody WmArticleListDto dto, @RequestParam("id") Integer id,
                                     @RequestParam(value = "orderType", required = false) String orderType) throws ParseException;
}
