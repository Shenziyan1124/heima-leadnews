package com.heima.apis.user.fallback;

import com.heima.apis.user.IUserClient;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmUser;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IUserClientFallback implements FallbackFactory<IUserClient> {

    @Override
    public IUserClient create(Throwable throwable) {
        return new IUserClient() {

            @Override
            public ResponseResult checkFollow(Integer userId, Long followId) {
                log.error("feign用户服务调用异常：{}", throwable.getMessage());
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"用户服务调用异常");
            }

            @Override
            public ResponseResult getById(Integer userId) {
                log.error("feign用户服务调用异常：{}", throwable.getMessage());
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"用户服务调用异常");
            }
        };
    }
}
