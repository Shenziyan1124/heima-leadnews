package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.RealNameListDto;
import com.heima.model.user.pojos.ApUserRealname;

public interface ApRealNameService extends IService<ApUserRealname> {
    /**
     * 实名认证列表
     * @param dto
     * @return
     */
    ResponseResult realNameList(RealNameListDto dto);

    /**
     * 实名认证通过
     * @param dto
     * @return
     */
    ResponseResult authPass(RealNameListDto dto);

    /**
     * 实名认证失败
     * @param dto
     * @return
     */
    ResponseResult authFail(RealNameListDto dto);
}
