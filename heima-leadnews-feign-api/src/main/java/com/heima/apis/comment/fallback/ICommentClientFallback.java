package com.heima.apis.comment.fallback;

import com.heima.apis.comment.ICommentClient;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmCommentListDto;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ICommentClientFallback implements FallbackFactory<ICommentClient> {
    @Override
    public ICommentClient create(Throwable cause) {
        return new ICommentClient() {
            @Override
            public PageResponseResult findNewsComments(WmCommentListDto dto) {
                log.error("调用 comment 服务失败: {}", dto, cause);
                PageResponseResult result = new PageResponseResult();
                result.setTotal(0);
                return result;
            }
        };
    }
}
