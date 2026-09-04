package com.heima.user.feign;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.apis.user.IUserClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUserFollow;
import com.heima.user.mapper.ApUserFollowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserClient implements IUserClient {

    @Autowired
    private ApUserFollowMapper apUserFollowMapper;

    @Override
    public ResponseResult checkFollow(Integer userId, Long followId) {
        Integer exists = apUserFollowMapper.selectCount(new QueryWrapper<ApUserFollow>()
                .eq("user_id", userId)
                .eq("follow_id", followId)
        );
        return ResponseResult.okResult(exists);
    }
}
