package com.heima.schedule.feign;

import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ScheduleClient implements IScheduleClient {

    @Autowired
    private TaskService taskService;

    /**
     * 添加任务
     *
     * @param task 任务对象
     * @return 任务id
     */
    @Override
    @PostMapping("/api/v1/task/add")
    public ResponseResult addTask(@RequestBody Task task){
        return ResponseResult.okResult(taskService.addTask(task));
    };

    /**
     * 取消任务
     *
     * @param taskId 任务id
     */
    @Override
    @GetMapping("/api/v1/task/{taskId}")
    public ResponseResult cancelTask(@PathVariable("taskId") long taskId){
        return ResponseResult.okResult(taskService.cancelTask(taskId));
    };

    /**
     * 获取任务
     *
     * @param type     任务类型
     * @param priority 优先级
     * @return 任务对象
     */
    @Override
    @GetMapping("/api/v1/task/{type}/{priority}")
    public ResponseResult pollTask(@PathVariable("type") int type,@PathVariable("priority") int priority){
        return ResponseResult.okResult(taskService.pollTask(type, priority));
    }
}
