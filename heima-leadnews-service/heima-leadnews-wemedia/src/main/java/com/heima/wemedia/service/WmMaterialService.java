package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialPageDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface WmMaterialService extends IService<WmMaterial> {

    /**
     * 上传图片
     * @param multipartFile
     * @return
     */
    ResponseResult uploadPicture(MultipartFile multipartFile) throws IOException;

    /**
     * 素材列表
     * @param dto
     * @return
     */
    ResponseResult listMaterial(WmMaterialPageDto dto);

    /**
     * 删除图片
     * @param id
     * @return
     */
    ResponseResult delPicture(Integer id);

    /**
     * 取消收藏
     * @param id
     * @return
     */
    ResponseResult cancelCollect(Integer id);

    /**
     * 收藏图片
     * @param id
     * @return
     */
    ResponseResult collect(Integer id);
}