package com.heima.wemedia.service;

import java.util.Date;

public interface WmNewsTaskService {
    /**
     * 添加文章任务到延迟队列中
     * @param id 文章id
     * @param publishTime 发布时间,可以作为任务的执行时间
     */
    public void addNewsTask(Integer id, Date publishTime);
}
