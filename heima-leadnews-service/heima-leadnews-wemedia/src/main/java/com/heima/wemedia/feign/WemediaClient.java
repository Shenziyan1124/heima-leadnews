package com.heima.wemedia.feign;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WemediaClient implements IWemediaClient {

    @Autowired
    private WmUserService wmUserService;
    @Autowired
    private WmUserMapper wmUserMapper;
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

    /**
     * 根据用户ID查询自媒体用户
     *
     * @param userId
     */
    @Override
    public ResponseResult getWmUserByUserId(Integer userId) {
        QueryWrapper<WmUser> wrapper = new QueryWrapper<>();
        wrapper.eq("id", userId);
        WmUser wmUser = wmUserMapper.selectOne(wrapper);
        return ResponseResult.okResult(wmUser);
    }
}
