package com.heima.comment.service;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmArticleCommentListDto;
import com.heima.model.wemedia.dtos.WmCommentListDto;

public interface ApCommentManageService {
    /**
     * 根据dto查询文章评论列表
     * @param dto
     * @return
     */
    ResponseResult findNewsComments(WmCommentListDto dto);

    /**
     * 根据dto查询文章详情评论列表
     * @param dto
     * @return
     */
    ResponseResult findCommentListByArticleId(WmArticleCommentListDto dto);
}
