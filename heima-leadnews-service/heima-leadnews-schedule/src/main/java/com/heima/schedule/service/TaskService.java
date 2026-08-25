package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;

/**
 * 对外访问接口
 */
public interface TaskService {

    /**
     * 添加任务
     * @param task   任务对象
     * @return       任务id
     */
    long addTask(Task task) ;

    /**
     * 取消任务
     * @param taskId   任务id
     */
    Boolean cancelTask(long taskId);

    /**
     * 获取任务
     * @param type     任务类型
     * @param priority 优先级
     * @return         任务对象
     */
    Task pollTask(int type,int priority);

}
