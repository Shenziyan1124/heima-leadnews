package com.heima.apis.article.fallback;

import com.heima.apis.article.IArticleClient;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
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
            /**
             * 保存文章
             * @param dto
             * @return
             */
            public ResponseResult saveArticle(ArticleDto dto) {
                log.error("调用 article 服务失败: {}", dto.toString(), cause);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "数据获取失败");
            }

            @Override
            public ResponseResult getArticleCountByChannelId(Integer id) {
                log.error("wemedia 调用 article-getArticleCountByChannelId 服务失败: {}", id);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "数据获取失败");
            }
        };
    }

//    public ResponseResult saveArticle(ArticleDto dto) {
//        log.error("调用 article 服务失败: {}", dto.toString());
//        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "数据获取失败");
//    }
}
