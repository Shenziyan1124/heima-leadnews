package com.heima.wemedia.controller.v1;


import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmArticleCommentListDto;
import com.heima.model.wemedia.dtos.WmCommentListDto;
import com.heima.model.wemedia.dtos.WmCommentReplyDto;
import com.heima.model.wemedia.dtos.WmCommentStatusDto;
import com.heima.wemedia.service.WmCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comment/manage")
@RequiredArgsConstructor
@Api(tags = "评论管理")
public class WmCommentController {

    private final WmCommentService wmCommentService;

    @PostMapping("/find_news_comments")
    @ApiOperation("文章评论列表")
    public ResponseResult findNewsComments(@RequestBody WmCommentListDto dto) {
        return wmCommentService.findNewsComments(dto);
    }

    @PostMapping("/update_comment_status")
    @ApiOperation("打开或关闭评论")
    public ResponseResult updateCommentStatus(@RequestBody WmCommentStatusDto dto) {
        return wmCommentService.updateCommentStatus(dto);
    }

    @PostMapping("/list")
    @ApiOperation("文章详情评论列表")
    public ResponseResult findCommentListByArticleId(@RequestBody WmArticleCommentListDto dto) {
        return wmCommentService.findCommentListByArticleId(dto);
    }

    @PostMapping("/comment_repay")
    @ApiOperation("作者回复评论")
    public ResponseResult commentRepay(@RequestBody WmCommentReplyDto dto) {
        return wmCommentService.commentReply(dto);
    }

}
