package com.heima.apis.comment;

import com.heima.apis.comment.fallback.ICommentClientFallback;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmArticleCommentListDto;
import com.heima.model.wemedia.dtos.WmCommentLikeDto;
import com.heima.model.wemedia.dtos.WmCommentListDto;
import com.heima.model.wemedia.dtos.WmCommentReplyDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.checkerframework.checker.units.qual.A;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "leadnews-comment", fallbackFactory = ICommentClientFallback.class)
public interface ICommentClient {

    @PostMapping("/api/v1/comment/manage/find_news_comments")
    @ApiOperation("查询所有文章评论的列表")
    ResponseResult findNewsComments(@RequestBody WmCommentListDto dto);

    @PostMapping("/api/v1/comment/manage/list")
    @ApiOperation("根据文章id查询评论列表")
    ResponseResult findCommentListByArticleId(@RequestBody WmArticleCommentListDto dto);

    @ApiOperation("评论回复")
    @PostMapping("/api/v1/comment/manage/comment_repay")
    ResponseResult commentReply(@RequestBody WmCommentReplyDto dto);

    @ApiOperation("作者评论点赞")
    @PostMapping("/api/v1/comment/manage/like")
    ResponseResult authorLike(@RequestBody WmCommentLikeDto dto);

    @ApiOperation("删除评论")
    @PostMapping("/api/v1/comment/manage/del_comment/{commentId}")
    ResponseResult delComment(@PathVariable("commentId") String commentId);

    @ApiOperation("删除评论回复")
    @PostMapping("/api/v1/comment/manage/del_comment_replay/{commentRepayId}")
    ResponseResult delCommentReplay(@PathVariable("commentRepayId") String commentRepayId);
}
