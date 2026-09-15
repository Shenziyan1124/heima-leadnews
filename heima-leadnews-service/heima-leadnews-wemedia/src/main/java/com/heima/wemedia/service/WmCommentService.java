package com.heima.wemedia.service;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.*;

public interface WmCommentService {

    /**
     * 评论列表
     * @param dto
     * @return
     */
    ResponseResult findNewsComments(WmCommentListDto dto);

    /**
     * 打开或关闭评论
     * @param dto
     * @return
     */
    ResponseResult updateCommentStatus(WmCommentStatusDto dto);

    /**
     * 根据文章id查询评论列表
     * @param dto
     * @return
     */
    ResponseResult findCommentListByArticleId(WmArticleCommentListDto dto);

    /**
     * 作者回复评论
     * @param dto
     * @return
     */
    ResponseResult commentReply(WmCommentReplyDto dto);

    /**
     * 作者评论点赞
     * @param dto
     * @return
     */
    ResponseResult authorLike(WmCommentLikeDto dto);

    /**
     * 删除评论
     * @param commentId
     * @return
     */
    ResponseResult delComment(String commentId);

    /**
     * 删除评论回复
     * @param commentRepayId
     * @return
     */
    ResponseResult delCommentReplay(String commentRepayId);
}
