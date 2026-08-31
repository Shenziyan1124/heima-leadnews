package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmChannelListDto;
import com.heima.model.wemedia.dtos.WmSensitiveDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.service.WmSensitiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
@Slf4j
public class WmSensitiveServiceImpl extends ServiceImpl<WmSensitiveMapper, WmSensitive> implements WmSensitiveService {
    /**
     * 获取敏感信息列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult getSensitiveList(WmChannelListDto dto) {
        // 1. 参数校验
        if (dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        dto.checkParam();

        // 2. 查询敏感信息列表
        IPage page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmSensitive> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(dto.getName()), WmSensitive::getSensitives, dto.getName());
        wrapper.orderByDesc(WmSensitive::getCreatedTime);
        page = page(page, wrapper);
        PageResponseResult pageResponseResult =
                new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());

        return pageResponseResult;
    }

    /**
     * 保存或更新敏感信息
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveOrUpdateSensitive(WmSensitiveDto dto) {
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 检查敏感词是否已存在
        LambdaQueryWrapper<WmSensitive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmSensitive::getSensitives, dto.getSensitives());
        WmSensitive existSensitive = getOne(wrapper);

        if (dto.getId() == null){
            // 新增：存在相同的敏感词则不能保存
            if (existSensitive != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "敏感词已存在");
            }
            WmSensitive wmSensitive = new WmSensitive();
            wmSensitive.setSensitives(dto.getSensitives());
            wmSensitive.setCreatedTime(new Date());
            save(wmSensitive);
        }else {
            // 修改
            WmSensitive wmSensitive = getById(dto.getId());
            if (wmSensitive == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
            }
            // 如果名称变了，检查新名称是否已存在
            if (!Objects.equals(wmSensitive.getSensitives(), dto.getSensitives())) {
                if (existSensitive != null) {
                    return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "敏感词已存在");
                }
                wmSensitive.setSensitives(dto.getSensitives());
                updateById(wmSensitive);
            }
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 根据id删除敏感信息
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult deleteById(Long id) {
        if (id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmSensitive byId = getById(id);
        if (byId == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
