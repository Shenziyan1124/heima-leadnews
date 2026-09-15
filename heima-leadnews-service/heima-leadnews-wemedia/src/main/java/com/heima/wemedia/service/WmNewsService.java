package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmAdminNewsReqDto;
import com.heima.model.wemedia.dtos.WmNewsDownOrUpDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsService extends IService<WmNews> {

    /**
     * 查询文章列表
     * @param dto
     * @return
     */
    ResponseResult findAll(WmNewsPageReqDto dto);

    /**
     * 发布/修改/保存草稿文章
     * @param dto
     * @return
     */
    ResponseResult submitNews(WmNewsDto dto);

    /**
     * 获取文章信息
     * @param id
     * @return
     */
    ResponseResult getNewsDetail(Integer id);

    /**
     * 删除文章
     * @param id
     * @return
     */
    ResponseResult delNews(Integer id);

    /**
     * 文章上下架
     * @param dto
     * @return
     */
    ResponseResult downOrUp(WmNewsDownOrUpDto dto);

    /**
     * 查询admin端文章列表
     * @param dto
     * @return
     */
    ResponseResult findAdminList(WmAdminNewsReqDto dto);

    /**
     * 获取admin端文章详情
     * @param id
     * @return
     */
    ResponseResult getAdminNewsDetail(Integer id);

    /**
     * admin文章审核失败
     * @param dto
     * @return
     */
    ResponseResult adminAuthFail(WmAdminNewsReqDto dto);

    /**
     * admin文章审核通过
     * @param dto
     * @return
     */
    ResponseResult adminAuthPass(WmAdminNewsReqDto dto);
}
