package com.heima.apis.comment;

import com.heima.apis.comment.fallback.ICommentClientFallback;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.wemedia.dtos.WmCommentListDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "leadnews-comment", fallbackFactory = ICommentClientFallback.class)
public interface ICommentClient {

    @PostMapping("/api/v1/comment/manage/find_news_comments")
    PageResponseResult findNewsComments(@RequestBody WmCommentListDto dto);
}
