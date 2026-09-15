package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * 对外访问接口
 */
@Service
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskinfoMapper taskinfoMapper;
    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    @Autowired
    private CacheService cacheService;

    /**
     * 添加任务
     *
     * @param task 任务对象
     * @return 任务id
     */
    @Override
    public long addTask(Task task) {

        // 1.添加任务到数据库中
        boolean success = addTaskToDb(task);
        if (success) {
            // 2.添加任务到redis中
            addTaskCache(task);
        }

        return task.getTaskId();

    }

    /**
     * 添加任务到redis中
     *
     * @param task
     */
    private void addTaskCache(Task task) {

        String key = task.getTaskType() + "_" + task.getPriority();

        // 获取预设时间 5分钟
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);

        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            // 2.1 任务执行时间小于当前时间,存入list
            cacheService.lLeftPush(ScheduleConstants.TOPIC + key, JSON.toJSONString(task));
        } else if (task.getExecuteTime() <= calendar.getTimeInMillis()) {
            // 2.2 任务执行时间大于当前时间 && 小于预设时间(x分钟),存入zset中
            cacheService.zAdd(
                    ScheduleConstants.FUTURE + key,
                    JSON.toJSONString(task),
                    task.getExecuteTime()
            );
        }


    }

    /**
     * 添加任务到数据库中
     *
     * @param task
     * @return
     */
    private boolean addTaskToDb(Task task) {

        // 保存任务表
        Taskinfo taskinfo = new Taskinfo();
        BeanUtils.copyProperties(task, taskinfo);
        taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
        log.info("插入taskinfo前: taskId={}", taskinfo.getTaskId());
        taskinfoMapper.insert(taskinfo);
        log.info("插入taskinfo后: taskId={}", taskinfo.getTaskId());

        // 设置任务id
        task.setTaskId(taskinfo.getTaskId());

        // 保存任务日志表
        TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
        BeanUtils.copyProperties(taskinfo, taskinfoLogs);
        taskinfoLogs.setVersion(1);
        taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
        log.info("插入taskinfoLogs前: taskId={}", taskinfoLogs.getTaskId());
        taskinfoLogsMapper.insert(taskinfoLogs);
        log.info("插入taskinfoLogs后: taskId={}", taskinfoLogs.getTaskId());

        return true;
    }

    /**
     * 取消任务
     *
     * @param taskId 任务id
     */
    @Override
    public Boolean cancelTask(long taskId) {
        boolean flag = false;

        // 删除任务,更新任务日志
        Task task = updateDB(taskId, ScheduleConstants.CANCELLED);

        // 删除Redis的数据 list/zset
        if (task != null) {
            removeTaskFromCache(task);
            flag = true;
        }
        return flag;
    }

    /**
     * 从缓存中删除任务
     *
     * @param task
     */
    private void removeTaskFromCache(Task task) {

        String key = task.getTaskType() + "_" + task.getPriority();
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lRemove(
                    ScheduleConstants.TOPIC + key,
                    0, // 删除所有匹配的元素
                    JSON.toJSONString(task));
        } else {
            cacheService.zRemove(
                    ScheduleConstants.FUTURE + key,
                    JSON.toJSONString(task)
            );
        }

    }

    /**
     * 更新任务状态
     *
     * @param taskId
     * @param status
     * @return
     */
    private Task updateDB(long taskId, int status) {
        // 删除任务
        taskinfoMapper.deleteById(taskId);
        // 更新任务日志
        TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
        taskinfoLogs.setStatus(status);
        taskinfoLogsMapper.updateById(taskinfoLogs);

        Task task = new Task();
        BeanUtils.copyProperties(taskinfoLogs, task);
        // 数据库存的是date,需要转换成毫秒值long
        task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        return task;
    }


    /**
     * 获取任务
     *
     * @param type     任务类型
     * @param priority 优先级
     * @return 任务对象
     */
    @Override
    public Task pollTask(int type, int priority) {

        Task task = null;

        try {
            String key = type + "_" + priority;

            // 从redis中拉去数据 pop
            String taskJson = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
            if (StringUtils.isNotBlank(taskJson)) {
                task = JSON.parseObject(taskJson, Task.class);

                // 修改数据库信息
                updateDB(task.getTaskId(), ScheduleConstants.EXECUTED);
            }
        } catch (Exception e) {
            log.error("获取任务失败", e);
            e.printStackTrace();
        }


        return task;
    }


    /**
     * 刷新任务 每分钟一次
     * 把redis中future中数据移到topic中
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void refreshTask() {

        String token = cacheService.tryLock("FEATURE_TASK_SYNC", 1000 * 30);
        // 如果锁成功
        if (StringUtils.isNotBlank(token)) {
            log.info("刷新任务");
            //获取未来所有数据的key
            Set<String> featureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            for (String featureKey : featureKeys) { // feature_100_50

                // 获取当前数据的key topic
                String topicKey = ScheduleConstants.TOPIC + featureKey.split(ScheduleConstants.FUTURE)[1];

                // 按照key和分值查询符合条件的数据
                Set<String> tasks = cacheService.zRangeByScore(featureKey, 0, System.currentTimeMillis());
                // 同步数据
                if (tasks != null && !tasks.isEmpty()) {
                    cacheService.refreshWithPipeline(featureKey, topicKey, tasks);
                    log.info("刷新任务成功,{},{}", tasks.size(), featureKey);
                }
            }
        }


    }

    /**
     * 数据库任务定时同步redis
     */
    @PostConstruct
    @Scheduled(cron = "0 */5 * * * ?")
    public void reloadData() {
        // 清理缓存中的数据
        clearCache();

        // 获取未来5分钟的任务
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        List<Taskinfo> taskInfos = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery()
                .lt(Taskinfo::getExecuteTime, calendar.getTime()));
        // 添加到redis中
        if (taskInfos != null && !taskInfos.isEmpty()) {
            for (Taskinfo taskinfo : taskInfos) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo, task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTaskCache(task);
            }
        }
        log.info("数据库任务同步到了redis");
    }

    public void clearCache() {
        Set<String> topicKeys = cacheService.scan(ScheduleConstants.TOPIC + "*");
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
        cacheService.delete(topicKeys);
        cacheService.delete(futureKeys);
    }
}
