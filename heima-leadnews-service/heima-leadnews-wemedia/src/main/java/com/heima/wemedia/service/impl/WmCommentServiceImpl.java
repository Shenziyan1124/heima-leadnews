package com.heima.wemedia.service.impl;

import com.heima.apis.article.IArticleClient;
import com.heima.apis.comment.ICommentClient;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.*;
import com.heima.wemedia.service.WmCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class WmCommentServiceImpl implements WmCommentService {

    private final ICommentClient iCommentClient;
    private final IArticleClient iArticleClient;

    /**
     * 评论列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findNewsComments(WmCommentListDto dto) {
        return iCommentClient.findNewsComments(dto);
    }

    /**
     * 打开或关闭评论
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult updateCommentStatus(WmCommentStatusDto dto) {
        if (dto == null || dto.getArticleId() == null ||
                dto.getOperation() == null || dto.getOperation() < 0 || dto.getOperation() > 1) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        Boolean isComment = (dto.getOperation() == 1);
        return iArticleClient.updateCommentStatus(dto.getArticleId(), isComment);
    }

    /**
     * 根据文章id查询评论列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findCommentListByArticleId(WmArticleCommentListDto dto) {
        if (dto == null || dto.getArticleId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        return iCommentClient.findCommentListByArticleId(dto);
    }

    /**
     * 作者回复评论
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult commentReply(WmCommentReplyDto dto) {
        if (dto == null || dto.getCommentId() == null || dto.getContent() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        return iCommentClient.commentReply(dto);
    }

    /**
     * 作者评论点赞
     *
     * @param dto
     * @return
     */
    public ResponseResult authorLike(WmCommentLikeDto dto) {
        if (dto == null || dto.getCommentId() == null ||
                dto.getOperation() == null || dto.getOperation() < 0 || dto.getOperation() > 1){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        return iCommentClient.authorLike(dto);
    }

    /**
     * 删除评论
     *
     * @param commentId
     * @return
     */
    @Override
    public ResponseResult delComment(String commentId) {
        if (commentId == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        return iCommentClient.delComment(commentId);
    }

    /**
     * 删除评论回复
     *
     * @param commentRepayId
     * @return
     */
    @Override
    public ResponseResult delCommentReplay(String commentRepayId) {
        if (commentRepayId == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        return iCommentClient.delCommentReplay(commentRepayId);
    }
}
