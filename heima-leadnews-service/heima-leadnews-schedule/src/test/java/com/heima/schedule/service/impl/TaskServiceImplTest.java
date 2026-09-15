package com.heima.schedule.service.impl;

import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.crypto.Data;

import java.util.Date;

import static org.junit.Assert.*;

@SpringBootTest(classes = com.heima.schedule.ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class TaskServiceImplTest {

    @Autowired
    private TaskService taskService;

    @Test
    public void addTask() {

        for (int i = 0; i < 5; i++){
            Task task = new Task();
            task.setTaskType(1+i);
            task.setPriority(1);
            task.setExecuteTime(new Date().getTime()+500*i);
            task.setParameters(("task_test_new Date().getTime()"+String.valueOf(500*i)).getBytes());

            long l = taskService.addTask(task);
            System.out.println(l);
        }
    }

    @Test
    public void cancelTask() {
        taskService.cancelTask(2092149629668999170L);
    }

    @Test
    public void pollTask() {
        Task task = taskService.pollTask(1, 1);
        System.out.println(task);
    }
}