package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.common.constants.RealNameStatusConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.RealNameListDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.user.mapper.ApRealNameMapper;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApRealNameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ApRealNameServiceImpl extends ServiceImpl<ApRealNameMapper, ApUserRealname> implements ApRealNameService {

    private final IWemediaClient wemediaClient;

    private final ApUserMapper apUserMapper;
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

        ApUserRealname realname = getById(dto.getId());
        ApUser apUser = apUserMapper.selectById(realname.getUserId());
        if (realname == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        // 先创建wemedia-user信息，成功后再更新状态
        WmUser wmUser = new WmUser();
        wmUser.setName(realname.getName());
        wmUser.setNickname(realname.getName());
        wmUser.setApUserId(realname.getUserId());
        wmUser.setSalt(apUser.getSalt());
        wmUser.setPassword(apUser.getPassword());
        wmUser.setPhone(apUser.getPhone());
        wmUser.setStatus(Integer.valueOf(RealNameStatusConstants.STATUS_SUCCESS));
        wmUser.setType(0);
        wmUser.setCreatedTime(new Date());

        ResponseResult result = wemediaClient.createWmUser(wmUser);
        if (result.getCode() != 200){
            return result;
        }

        // Feign成功后再更新实名认证状态
        realname.setStatus(RealNameStatusConstants.STATUS_SUCCESS);
        realname.setReason(RealNameStatusConstants.STATUS_SUCCESS_DESC);
        updateById(realname);

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
        // 参数校验
        if (dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 根据id查询实名认证信息
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
