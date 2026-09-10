package com.heima.comment.controller.v1;


import com.heima.comment.service.ApCommentService;
import com.heima.model.comment.dtos.CommentLikeListDto;
import com.heima.model.comment.dtos.CommentLikeBehaviorDto;
import com.heima.model.comment.dtos.CommentLikeSaveDto;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comment")
@RequiredArgsConstructor
@Api(tags = "APP端评论")
public class ApCommentController {

    private final ApCommentService apCommentService;

    @PostMapping("/save")
    @ApiOperation("保存评论")
    public ResponseResult saveComment(@RequestBody CommentLikeSaveDto dto) {
        return apCommentService.saveComment(dto);
    }

    @PostMapping("/load")
    @ApiOperation("加载评论列表")
    public ResponseResult loadComment(@RequestBody CommentLikeListDto dto) {
        return apCommentService.loadComment(dto);
    }

    @PostMapping("/like")
    @ApiOperation("点赞评论")
    public ResponseResult likeComment(@RequestBody CommentLikeBehaviorDto dto) {
        return apCommentService.likeComment(dto);
    }
}
