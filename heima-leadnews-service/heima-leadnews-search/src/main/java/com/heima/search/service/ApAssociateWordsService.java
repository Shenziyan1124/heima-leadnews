package com.heima.search.service;


import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

/**
 * <p>
 * 联想词表 服务类
 * </p>
 *
 * @author itheima
 */


public interface ApAssociateWordsService  {

    /**
     * 联想词搜索
     * @param dto
     * @return
     */
    ResponseResult search(UserSearchDto dto);
}