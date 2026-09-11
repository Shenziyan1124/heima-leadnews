package com.heima.wemedia.controller.v1;


import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.wemedia.dtos.WmCommentListDto;
import com.heima.wemedia.service.WmCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comment/manage")
@RequiredArgsConstructor
@Api(tags = "评论管理")
public class WmCommentController {

    private final WmCommentService wmCommentService;

    @PostMapping("/find_news_comments")
    @ApiOperation("评论列表")
    public PageResponseResult findNewsComments(WmCommentListDto dto) {
        return wmCommentService.findNewsComments(dto);
    }
}
