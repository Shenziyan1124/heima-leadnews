package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelListDto;
import com.heima.model.wemedia.dtos.WmSensitiveDto;
import com.heima.model.wemedia.pojos.WmSensitive;

public interface WmSensitiveService extends IService<WmSensitive> {
    /**
     * 获取敏感信息列表
     * @param dto
     * @return
     */
    ResponseResult getSensitiveList(WmChannelListDto dto);

    /**
     * 保存或更新敏感信息
     * @param dto
     * @return
     */
    ResponseResult saveOrUpdateSensitive(WmSensitiveDto dto);

    /**
     * 根据id删除敏感信息
     * @param id
     * @return
     */
    ResponseResult deleteById(Long id);
}
