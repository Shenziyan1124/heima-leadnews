package com.heima.xxljob.job;


import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HelloJob {
    @XxlJob("demoJobHandler")
    public void execute() {
        System.out.println("Hello Job!");
    }

    @XxlJob("shardingJobHandler")
    public void execute1() {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        List<Integer> list = getList();
        for (Integer i : list) {
            if (i % shardTotal == shardIndex){
                System.out.println("分片参数：当前分片下标 = " + shardIndex + ",任务项:" + i);
            }
        }
    }
    
    public List<Integer> getList(){
        List<Integer> list = new ArrayList();
        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }
        return list;
    }
}
