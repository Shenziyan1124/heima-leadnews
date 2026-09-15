package com.heima.user.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.user.pojos.ApUserFollow;
import com.heima.user.mapper.ApUserFollowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class FollowDataSyncSchedule {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ApUserFollowMapper apUserFollowMapper;

    /**
     * 定时同步Redis关注数据到MySQL
     * 每5分钟执行一次，初始延迟40秒（避免与其他定时任务冲突）
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 40000)
    public void syncFollowData() {
        log.info("开始同步Redis关注数据到MySQL...");

        try {
            // 扫描所有 follow:user:* 的key
            Set<String> keys = cacheService.keys(ApUserBehaviorConstants.FOLLOW_USER_KEY + "*");

            if (keys == null || keys.isEmpty()) {
                log.info("没有需要同步的关注数据");
                return;
            }

            int syncCount = 0;
            for (String key : keys) {
                // 提取userId
                String userIdStr = key.replace(ApUserBehaviorConstants.FOLLOW_USER_KEY, "");
                Integer userId;
                try {
                    userId = Integer.parseInt(userIdStr);
                } catch (NumberFormatException e) {
                    log.warn("无效的userId: {}", userIdStr);
                    continue;
                }

                // 获取Redis中的关注列表（使用sScan）
                Set<String> followIds = new HashSet<>();
                ScanOptions options = ScanOptions.scanOptions().match("*").count(100).build();
                try (Cursor<String> cursor = cacheService.sScan(key, options)) {
                    while (cursor.hasNext()) {
                        followIds.add(cursor.next());
                    }
                }

                if (followIds.isEmpty()) {
                    continue;
                }

                for (String followIdStr : followIds) {
                    Integer followId;
                    try {
                        followId = Integer.parseInt(followIdStr);
                    } catch (NumberFormatException e) {
                        log.warn("无效的followId: {}", followIdStr);
                        continue;
                    }

                    // 检查MySQL中是否已存在
                    QueryWrapper<ApUserFollow> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("user_id", userId);
                    queryWrapper.eq("follow_id", followId);
                    ApUserFollow existRecord = apUserFollowMapper.selectOne(queryWrapper);

                    if (existRecord == null) {
                        // MySQL中不存在，插入
                        ApUserFollow record = new ApUserFollow();
                        record.setUserId(userId);
                        record.setFollowId(followId);
                        record.setFollowName(""); // 名称将在后续更新
                        record.setLevel((short) 0);
                        record.setIsNotice((short) 0);
                        record.setCreatedTime(new Date());
                        apUserFollowMapper.insert(record);
                        syncCount++;
                        log.info("同步关注记录: userId={}, followId={}", userId, followId);
                    }
                }
            }

            log.info("关注数据同步完成，共同步{}条记录", syncCount);
        } catch (Exception e) {
            log.error("同步关注数据失败: {}", e.getMessage(), e);
        }
    }
}
