package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.apis.article.IArticleClient;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmChannelDto;
import com.heima.model.wemedia.dtos.WmChannelslistDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.WmChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@Transactional
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {

    @Autowired
    private IArticleClient articleClient;


    /**
     * 查询所有频道
     *
     * @return
     */
    @Override
    public ResponseResult findAllChannel() {
        return ResponseResult.okResult(list());
    }

    /**
     * 频道名称模糊分页查询
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult filterList(WmChannelslistDto dto) {
        // 1. 参数判断
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        dto.checkParam();

        // 2. 分页查询
        IPage page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(dto.getName()), WmChannel::getName, dto.getName());
        wrapper.orderByDesc(WmChannel::getCreatedTime);
        page = page(page, wrapper);
        PageResponseResult pageResponseResult =
                new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());

        return pageResponseResult;
    }

    /**
     * 新增或修改频道
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveOrUpdateChannel(WmChannelDto dto) {
        // 1. 参数判断
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        if (dto.getId() == null){
            // 新增
            WmChannel wmChannel = new WmChannel();
            BeanUtils.copyProperties(dto, wmChannel);
            wmChannel.setCreatedTime(new Date());

            // 2.频道名词不能重复
            ResponseResult validateResult = validateNameIsExist(wmChannel);
            if (validateResult.getCode() != AppHttpCodeEnum.SUCCESS.getCode())
                return validateResult;

            save(wmChannel);
        }else {
            // 修改

            // 1.根据id查询
            WmChannel wmChannel = getById(dto.getId());
            if (wmChannel == null)
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
            BeanUtils.copyProperties(dto, wmChannel);

            // 2.频道名词不能重复
            ResponseResult validateResult = validateNameIsExist(wmChannel);
            if (validateResult.getCode() != AppHttpCodeEnum.SUCCESS.getCode())
                return validateResult;

            updateById(wmChannel);
        }
        
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 删除频道
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult deleteChannel(Integer id) {
        // 1. 参数判断
        if (id == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 2. 根据id查询,判断频道状态
        WmChannel wmChannel = getById(id);
        if (Boolean.TRUE.equals(wmChannel.getStatus())){
            return ResponseResult.errorResult(
                    AppHttpCodeEnum.CHANNEL_DATA_NOT_DELETE,"频道数据不能删除,必须是禁用的状态");
        }

        // 3. 删除
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 验证频道名称是否存在
     * @param wmChannel
     * @return
     */
    private ResponseResult validateNameIsExist(WmChannel wmChannel) {
        LambdaQueryWrapper<WmChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmChannel::getName, wmChannel.getName());
        WmChannel wmChannelByName = getOne(wrapper);

        // 新增修改的逻辑 - 频道名称不能重复
        if (wmChannelByName != null && !wmChannelByName.getId().equals(wmChannel.getId()))
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST);

        // 修改的逻辑 - 频道名称不能名称重复&&频道被引用则不能禁用
        if (wmChannel.getId() != null && wmChannelByName != null) {
            ResponseResult result = articleClient.getArticleCountByChannelId(wmChannel.getId());
            if (result.getCode().equals(AppHttpCodeEnum.SUCCESS.getCode()) && (Integer) result.getData() > 0){
                return ResponseResult.errorResult(AppHttpCodeEnum.ARTICLE_COUNT_ERROR);
            }
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
