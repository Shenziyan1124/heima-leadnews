package com.heima.wemedia.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsAutoScanService {

    /**
     * 自动审核文章
     * @param id 文章id
     */
    void autoScanWmNews(Integer id);


    ResponseResult saveAppArticle(WmNews wmNews);
}
