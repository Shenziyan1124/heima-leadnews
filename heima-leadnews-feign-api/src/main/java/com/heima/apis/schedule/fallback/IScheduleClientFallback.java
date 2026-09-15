package com.heima.apis.schedule.fallback;

import com.heima.apis.article.IArticleClient;
import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.schedule.dtos.Task;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IScheduleClientFallback implements FallbackFactory<IScheduleClient> {
    @Override
    public IScheduleClient create(Throwable throwable) {
        return new IScheduleClient() {

            /**
             * 添加任务
             *
             * @param task 任务对象
             * @return 任务id
             */
            @Override
            public ResponseResult addTask(Task task) {
                log.error("调用 schedule 服务失败: {}", task.toString());
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
            }

            /**
             * 取消任务
             *
             * @param taskId 任务id
             */
            @Override
            public ResponseResult cancelTask(long taskId) {
                log.error("调用 schedule 服务失败: {}", taskId);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
            }

            /**
             * 获取任务
             *
             * @param type     任务类型
             * @param priority 优先级
             * @return 任务对象
             */
            @Override
            public ResponseResult pollTask(int type, int priority) {
                log.error("调用 schedule 服务失败: {} {}", type, priority);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
            }
        };

    }

}
