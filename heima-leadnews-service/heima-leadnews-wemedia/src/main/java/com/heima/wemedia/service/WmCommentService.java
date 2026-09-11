package com.heima.wemedia.service;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.wemedia.dtos.WmCommentListDto;

public interface WmCommentService {

    /**
     * 评论列表
     * @param dto
     * @return
     */
    PageResponseResult findNewsComments(WmCommentListDto dto);
}
