package com.heima.comment.service;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.wemedia.dtos.WmCommentListDto;

public interface ApCommentManageService {
    PageResponseResult findNewsComments(WmCommentListDto dto);
}
