package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelDto;
import com.heima.model.wemedia.dtos.WmChannelListDto;
import com.heima.model.wemedia.pojos.WmChannel;

public interface WmChannelService extends IService<WmChannel> {

    /**
     * 查询所有频道
     * @return
     */
    ResponseResult findAllChannel();
    /**
     * 频道名称模糊分页查询
     * @param dto
     * @return
     */
    ResponseResult filterList(WmChannelListDto dto);

    /**
     * 新增或修改频道
     * @param dto
     * @return
     */
    ResponseResult saveOrUpdateChannel(WmChannelDto dto);

    /**
     * 删除频道
     * @param id
     * @return
     */
    ResponseResult deleteChannel(Integer id);
}
