package com.heima.wemedia.feign;

import com.heima.apis.wemedia.IWemediaClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.service.WmUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WemediaClient implements IWemediaClient {

    @Autowired
    private WmUserService wmUserService;
    /**
     * 创建自媒体用户
     *
     * @param wmUser
     * @return
     */
    @Override
    public ResponseResult createWmUser(@RequestBody WmUser wmUser) {
        return wmUserService.createWmUser(wmUser);
    }
}
