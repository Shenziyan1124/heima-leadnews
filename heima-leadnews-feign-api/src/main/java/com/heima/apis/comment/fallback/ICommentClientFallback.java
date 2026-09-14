package com.heima.apis.comment.fallback;

import com.heima.apis.comment.ICommentClient;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmArticleCommentListDto;
import com.heima.model.wemedia.dtos.WmCommentLikeDto;
import com.heima.model.wemedia.dtos.WmCommentListDto;
import com.heima.model.wemedia.dtos.WmCommentReplyDto;
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
            public ResponseResult findNewsComments(WmCommentListDto dto) {
                log.error("调用 comment 服务失败: {}", dto, cause);
                PageResponseResult result = new PageResponseResult();
                result.setTotal(0);
                return result;
            }

            @Override
            public ResponseResult findCommentListByArticleId(WmArticleCommentListDto dto) {
                log.error("调用 comment 服务失败,获取文章评论列表: {}", dto, cause);
                PageResponseResult result = new PageResponseResult();
                result.setTotal(0);
                return result;
            }

            @Override
            public ResponseResult commentReply(WmCommentReplyDto dto) {
                log.error("调用 comment 服务失败,评论回复: {}", dto, cause);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
            }

            @Override
            public ResponseResult authorLike(WmCommentLikeDto dto) {
                log.error("调用 comment 服务失败,作者评论点赞: {}", dto, cause);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
            }

            @Override
            public ResponseResult delComment(String commentId) {
                log.error("调用 comment 服务失败,删除评论: {}", commentId, cause);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
            }

            @Override
            public ResponseResult delCommentReplay(String commentRepayId) {
                log.error("调用 comment 服务失败,删除评论回复: {}", commentRepayId, cause);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
            }
        };
    }
}
