package com.heima.wemedia.service.impl;

import com.heima.apis.comment.ICommentClient;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.wemedia.dtos.WmCommentListDto;
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

    /**
     * 评论列表
     *
     * @param dto
     * @return
     */
    @Override
    public PageResponseResult findNewsComments(WmCommentListDto dto) {
        return iCommentClient.findNewsComments(dto);
    }
}
