package com.heima.wemedia.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmArticleListDto;

import java.text.ParseException;

public interface WmStatisticsService {
    /**
     * 根据时间维度查询图文数据
     * @param beginDate
     * @param endDate
     * @return
     */
    ResponseResult getNewsDimension(String beginDate, String endDate) throws ParseException;

    /**
     * 根据作者查询图文数据
     * @param dto
     * @return
     */
    ResponseResult getAuthorNewsPage(WmArticleListDto dto);
}
