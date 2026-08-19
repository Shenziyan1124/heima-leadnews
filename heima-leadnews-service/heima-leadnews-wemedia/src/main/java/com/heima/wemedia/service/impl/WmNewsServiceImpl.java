package com.heima.wemedia.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl  extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {


    /**
     * 查询文章列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findAll(WmNewsPageReqDto dto) {

        // 1. 参数校验
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        dto.checkParam();

        // 2. 获取user
        WmUser user = WmThreadLocalUtil.getUser();
        if (user == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);

        // 3. 分页查询
        IPage page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper
                .eq(WmNews::getUserId, user.getId())
                .eq(dto.getStatus() != null, WmNews::getStatus, dto.getStatus())
                .eq(dto.getChannelId() != null, WmNews::getChannelId, dto.getChannelId())
                .like(StringUtils.isNotBlank(dto.getKeyword()), WmNews::getTitle, dto.getKeyword())
                .between(dto.getBeginPubDate() != null && dto.getEndPubDate() != null,
                        WmNews::getPublishTime, dto.getBeginPubDate(), dto.getEndPubDate())
                //.ge(dto.getBeginPubDate() != null, WmNews::getPublishTime, dto.getBeginPubDate())
                //.le(dto.getEndPubDate() != null, WmNews::getPublishTime, dto.getEndPubDate())
                .orderByDesc(WmNews::getPublishTime);
        IPage result = page(page, lambdaQueryWrapper);
        log.info("result: {}", result);

        page = page(page, lambdaQueryWrapper);

        PageResponseResult pageResponseResult =
                new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());

        return pageResponseResult;
    }
}
