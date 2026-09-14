package com.heima.wemedia.service;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmArticleCommentListDto;
import com.heima.model.wemedia.dtos.WmCommentListDto;
import com.heima.model.wemedia.dtos.WmCommentReplyDto;
import com.heima.model.wemedia.dtos.WmCommentStatusDto;

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
}
