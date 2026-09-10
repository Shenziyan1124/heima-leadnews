package com.heima.comment.service;

import com.heima.model.comment.dtos.CommentLikeListDto;
import com.heima.model.comment.dtos.CommentLikeBehaviorDto;
import com.heima.model.comment.dtos.CommentLikeSaveDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApCommentService {

    /**
     * 保存评论
     * @param dto
     */
    ResponseResult saveComment(CommentLikeSaveDto dto);

    /**
     * 加载评论列表
     * @param dto
     * @return
     */
    ResponseResult loadComment(CommentLikeListDto dto);

    /**
     * 点赞评论
     * @param dto
     * @return
     */
    ResponseResult likeComment(CommentLikeBehaviorDto dto);
}
