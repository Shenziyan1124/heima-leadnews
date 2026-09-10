package com.heima.comment.service;

import com.heima.model.comment.dtos.*;
import com.heima.model.common.dtos.ResponseResult;

public interface ApCommentRepayService {

    /**
     * 保存回复
     * @param dto
     */
    ResponseResult saveCommentRepay(CommentRepaySaveDto dto);

    /**
     * 加载回复列表
     * @param dto
     * @return
     */
    ResponseResult loadCommentRepay(CommentRepayListDto dto);

    /**
     * 点赞回复
     * @param dto
     * @return
     */
    ResponseResult likeCommentRepay(CommentRepayLikeBehaviorDto dto);
}
