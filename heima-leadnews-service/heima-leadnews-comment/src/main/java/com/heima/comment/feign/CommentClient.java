package com.heima.comment.feign;

import com.heima.apis.comment.ICommentClient;
import com.heima.comment.service.ApCommentManageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.wemedia.dtos.WmArticleCommentListDto;
import com.heima.model.wemedia.dtos.WmCommentLikeDto;
import com.heima.model.wemedia.dtos.WmCommentListDto;
import com.heima.model.wemedia.dtos.WmCommentReplyDto;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentClient implements ICommentClient {

    @Autowired
    private ApCommentManageService apCommentManageService;

    @Override
    public ResponseResult findNewsComments(WmCommentListDto dto) {
        return apCommentManageService.findNewsComments(dto);
    }

    @Override
    public ResponseResult findCommentListByArticleId(WmArticleCommentListDto dto) {
        return apCommentManageService.findCommentListByArticleId(dto);
    }

    @Override
    public ResponseResult commentReply(WmCommentReplyDto dto) {
        return apCommentManageService.commentReply(dto);
    }

    @Override
    public ResponseResult authorLike(WmCommentLikeDto dto) {

        return apCommentManageService.authorLike(dto);
    }
}
