package com.heima.apis.wemedia;


import com.heima.apis.wemedia.fallback.IWemediaClientFallback;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "leadnews-wemedia",fallbackFactory = IWemediaClientFallback.class)
public interface IWemediaClient {


    /**
     * 创建自媒体用户
     * @return
     */
    @PostMapping("/api/v1/user/create")
    ResponseResult createWmUser(@RequestBody WmUser wmUser);

    /**
     * 根据用户ID查询自媒体用户
     */
    @PostMapping("/api/v1/user/getByUserId")
    ResponseResult getWmUserByUserId(@RequestBody Integer userId);

}
