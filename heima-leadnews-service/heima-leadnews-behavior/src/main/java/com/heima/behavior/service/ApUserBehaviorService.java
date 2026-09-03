package com.heima.behavior.service;

import com.heima.model.behavior.dtos.CollectionBehaviorDto;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApUserBehaviorService {

    /**
     * 用户点赞行为
     * @param dto
     * @return
     */
    ResponseResult saveLikesBehavior(LikesBehaviorDto dto);

    /**
     * 用户阅读行为
     * @param dto
     * @return
     */
    ResponseResult saveReadBehavior(ReadBehaviorDto dto);

    /**
     * 用户不喜欢行为
     * @param dto
     * @return
     */
    ResponseResult saveUnLikeBehavior(UnLikesBehaviorDto dto);

    /**
     * 用户收藏行为
     * @param dto
     * @return
     */
    ResponseResult saveCollectionBehavior(CollectionBehaviorDto dto);
}
