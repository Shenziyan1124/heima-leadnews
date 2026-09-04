package com.heima.apis.user;

import com.heima.apis.user.fallback.IUserClientFallback;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "leadnews-user", fallbackFactory = IUserClientFallback.class)
public interface IUserClient {

    @GetMapping("/api/v1/user/checkFollow")
    ResponseResult checkFollow(
            @RequestParam("userId") Integer userId,
            @RequestParam("followId") Long followId);

}
