package com.heima.comment.feign;

import com.heima.apis.comment.ICommentClient;
import com.heima.comment.service.ApCommentManageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.wemedia.dtos.WmCommentListDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentClient implements ICommentClient {

    @Autowired
    private ApCommentManageService apCommentManageService;

    @Override
    public PageResponseResult findNewsComments(WmCommentListDto dto) {
        return apCommentManageService.findNewsComments(dto);
    }
}
