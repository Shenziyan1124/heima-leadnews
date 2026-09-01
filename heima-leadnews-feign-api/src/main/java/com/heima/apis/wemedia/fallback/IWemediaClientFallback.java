package com.heima.apis.wemedia.fallback;

import com.heima.apis.schedule.IScheduleClient;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmUser;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IWemediaClientFallback implements FallbackFactory<IWemediaClient> {

    @Override
    public IWemediaClient create(Throwable throwable) {
        return new IWemediaClient() {
            /**
             * 创建自媒体用户
             *
             * @param wmUser
             * @return
             */
            @Override
            public ResponseResult createWmUser(WmUser wmUser) {
                log.error("调用 wemedia 服务创建用户失败: wmUser={}, cause={}", wmUser, throwable.getMessage(), throwable);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "调用 wemedia 服务失败");
            }
        };
    }
}
