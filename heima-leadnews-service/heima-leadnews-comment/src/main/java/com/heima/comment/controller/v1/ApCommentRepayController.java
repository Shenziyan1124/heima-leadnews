package com.heima.comment.controller.v1;


import com.heima.comment.service.ApCommentRepayService;
import com.heima.model.comment.dtos.*;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comment_repay")
@RequiredArgsConstructor
@Api(tags = "APP端评论回复")
public class ApCommentRepayController {

    private final ApCommentRepayService apCommentRepayService;

    @PostMapping("/save")
    @ApiOperation("保存回复")
    public ResponseResult saveComment(@RequestBody CommentRepaySaveDto dto) {
        return apCommentRepayService.saveCommentRepay(dto);
    }

    @PostMapping("/load")
    @ApiOperation("加载回复列表")
    public ResponseResult loadComment(@RequestBody CommentRepayListDto dto) {
        return apCommentRepayService.loadCommentRepay(dto);
    }

    @PostMapping("/like")
    @ApiOperation("点赞回复")
    public ResponseResult likeComment(@RequestBody CommentRepayLikeBehaviorDto dto) {
        return apCommentRepayService.likeCommentRepay(dto);
    }
}
