package com.heima.apis.article.fallback;

import com.heima.apis.article.IArticleClient;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmArticleListDto;
import lombok.extern.slf4j.Slf4j;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IArticleClientFallback implements FallbackFactory<IArticleClient> {
    @Override
    public IArticleClient create(Throwable cause) {
        return new IArticleClient() {
            @Override
            public ResponseResult saveArticle(ArticleDto dto) {
                log.error("调用 article 服务失败: {}", dto.toString(), cause);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "数据获取失败");
            }

            @Override
            public ResponseResult getArticleCountByChannelId(Integer id) {
                log.error("wemedia 调用 article-getArticleCountByChannelId 服务失败: {}", id);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "数据获取失败");
            }

            @Override
            public ResponseResult getCommentStatus(Long articleId) {
                log.error("调用 article-getCommentStatus 服务失败: {}", articleId, cause);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "数据获取失败");
            }

            @Override
            public ResponseResult updateCommentStatus(Long articleId, Boolean isComment) {
                log.error("调用 article-updateCommentStatus 服务失败: {} {}", articleId, isComment, cause);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "数据获取失败");
            }

            @Override
            public ResponseResult getNewsDimension(String beginDate, String endDate, Integer id) {
                log.error("调用 article-getNewsDimension 服务失败: {} {} {}", beginDate, endDate, id, cause);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "数据获取失败");
            }

            /**
             * 获取作者文章分页列表
             *
             * @param dto
             * @param id
             * @param orderType
             * @return
             */
            @Override
            public ResponseResult getAuthorNewsPage(WmArticleListDto dto, Integer id, String orderType) {
                log.error("调用 article-getAuthorNewsPage 服务失败: {} {} {}", dto.toString(), id, orderType, cause);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "数据获取失败");
            }

        };
    }
}
