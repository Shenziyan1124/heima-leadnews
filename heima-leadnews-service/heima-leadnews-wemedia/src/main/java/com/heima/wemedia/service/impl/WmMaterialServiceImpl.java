package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WmMaterialConstants;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialPageDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@Transactional
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private WmNewsMaterialMapper WmNewsMaterialMapper;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @return
     */
    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) throws IOException {
        // 1.校验参数
        if (multipartFile == null || multipartFile.getSize() == 0)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        // 2.上传到minio中
        String fileName = UUID.randomUUID().toString().replace("-", "");
        // aa.jpg
        String originalFilename = multipartFile.getOriginalFilename();
        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));

        String url = null;
        try {
            url = fileStorageService.uploadImgFile(
                    "", fileName + postfix, multipartFile.getInputStream());
            log.info("上传图片成功 {}", url);
        } catch (IOException e) {
            log.error("上传图片失败 {}", e.getMessage());
        }


        // 3.url保存到数据库
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUserId(WmThreadLocalUtil.getUser().getId());
        wmMaterial.setUrl(url);
        wmMaterial.setIsCollection(WmMaterialConstants.NOT_COLLECTED); // 0 表示未收藏 1 表示已收藏
        wmMaterial.setType(WmMaterialConstants.TYPE_IMAGE); // 0 表示图片 1 表示视频
        wmMaterial.setCreatedTime(new Date());
        save(wmMaterial);


        // 4.返回结果
        return ResponseResult.okResult(wmMaterial);
    }

    /**
     * 素材列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult listMaterial(WmMaterialPageDto dto) {


        Integer userId = WmThreadLocalUtil.getUser().getId();
        if (userId == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);

        // 1.参数校验
        dto.checkParam();

        // 2.分页查询
        IPage page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmMaterial::getUserId, userId);
        wrapper.eq(dto.getIsCollection() != null && dto.getIsCollection() == WmMaterialConstants.COLLECTED,
                WmMaterial::getIsCollection, dto.getIsCollection());
        wrapper.orderByDesc(WmMaterial::getCreatedTime);

        page = page(page, wrapper);


        //        Page<WmMaterial> result = lambdaQuery()
        //                .eq(WmMaterial::getUserId, userId)
        //                .eq(dto.getIsCollection() != null && dto.getIsCollection() == WmMaterialConstants.COLLECTED,
        //                        WmMaterial::getIsCollection, dto.getIsCollection())
        //                .orderByDesc(WmMaterial::getCreatedTime)
        //                .page(new Page<>(dto.getPage(), dto.getSize()));
        // 3.结果返回
        //        if (result.getRecords() == null) {
        //           return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "数据不存在");
        //        }

        PageResponseResult pageResponseResult =
                new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());

        return pageResponseResult;
    }

    /**
     * 删除图片
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult delPicture(Integer id) {
        if (id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"参数无效");
        }
        WmMaterial wmMaterial = getById(id);
        if (wmMaterial == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "数据不存在");
        }

        LambdaQueryWrapper<WmNewsMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmNewsMaterial::getMaterialId, id);
        Integer count = WmNewsMaterialMapper.selectCount(wrapper);
        if (count > 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "素材已被文章引用，不能删除");
        }


        // 删除图片
        fileStorageService.delete(wmMaterial.getUrl());
        // 删除数据库记录
       removeById(id);
       return  ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        //return null;
    }

    /**
     * 取消收藏
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult cancelCollect(Integer id) {
        if (id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"参数无效");
        }

        WmMaterial wmMaterial = getById(id);
        if (wmMaterial == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "数据不存在");

        // 取消收藏
        wmMaterial.setIsCollection(WmMaterialConstants.NOT_COLLECTED);
        updateById(wmMaterial);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 收藏图片
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult collect(Integer id) {
        if (id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"参数无效");
        }

        WmMaterial wmMaterial = getById(id);
        if (wmMaterial == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "数据不存在");

        // 收藏
        wmMaterial.setIsCollection(WmMaterialConstants.COLLECTED);
        updateById(wmMaterial);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

}
