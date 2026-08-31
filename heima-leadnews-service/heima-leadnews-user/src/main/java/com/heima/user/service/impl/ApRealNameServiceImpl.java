package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.RealNameStatusConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.RealNameListDto;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.user.mapper.ApRealNameMapper;
import com.heima.user.service.ApRealNameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@Transactional
public class ApRealNameServiceImpl extends ServiceImpl<ApRealNameMapper, ApUserRealname> implements ApRealNameService {
    /**
     * 实名认证列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult realNameList(RealNameListDto dto) {

        if (dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        dto.checkParam();

        IPage<ApUserRealname> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ApUserRealname> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getStatus() != null,ApUserRealname::getStatus, dto.getStatus());
        wrapper.orderByDesc(ApUserRealname::getUpdatedTime);
        page = page(page, wrapper);

        PageResponseResult pageResponseResult =
                new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());

        return pageResponseResult;
    }

    /**
     * 实名认证通过
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult authPass(RealNameListDto dto) {
        if (dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApUserRealname byId = getById(dto.getId());
        if (byId == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        byId.setStatus(RealNameStatusConstants.STATUS_SUCCESS);
        byId.setReason(RealNameStatusConstants.STATUS_SUCCESS_DESC);
        updateById(byId);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 实名认证失败
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult authFail(RealNameListDto dto) {
        if (dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUserRealname byId = getById(dto.getId());
        if (byId == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        byId.setStatus(RealNameStatusConstants.STATUS_FAIL);
        byId.setReason(dto.getMsg());
        updateById(byId);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
