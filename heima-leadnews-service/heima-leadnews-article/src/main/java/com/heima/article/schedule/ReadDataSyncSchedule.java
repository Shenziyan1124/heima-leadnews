package com.heima.article.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.pojos.ApArticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 定时对账：Redis阅读数据 → MySQL同步
 */
@Component
@Slf4j
public class ReadDataSyncSchedule {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ApArticleMapper apArticleMapper;

    /**
     * 每5分钟执行一次，同步Redis阅读数据到MySQL
     */
    @Scheduled(fixedRate = 300000)
    public void syncReadData() {
        log.info("开始同步Redis阅读数据到MySQL...");

        // 1. 扫描所有 read_count:article:* 的key
        Set<String> keys = cacheService.scan(ApUserBehaviorConstants.READ_COUNT_KEY + "*");

        if (keys == null || keys.isEmpty()) {
            log.info("Redis中没有阅读数据，跳过同步");
            return;
        }

        int syncCount = 0;

        for (String key : keys) {
            // key格式: read_count:article:{articleId}
            String articleIdStr = key.substring(ApUserBehaviorConstants.READ_COUNT_KEY.length());
            Long articleId;
            try {
                articleId = Long.parseLong(articleIdStr);
            } catch (NumberFormatException e) {
                log.warn("解析articleId失败: {}", articleIdStr);
                continue;
            }

            // 2. 获取Redis中的阅读次数
            String countStr = cacheService.get(key);
            if (countStr == null) {
                continue;
            }

            int redisCount;
            try {
                redisCount = Integer.parseInt(countStr);
            } catch (NumberFormatException e) {
                log.warn("解析阅读次数失败: {}", countStr);
                continue;
            }

            // 3. 查询MySQL当前views值
            LambdaQueryWrapper<ApArticle> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ApArticle::getId, articleId);
            queryWrapper.select(ApArticle::getId, ApArticle::getViews);
            ApArticle article = apArticleMapper.selectOne(queryWrapper);

            if (article == null) {
                log.warn("文章不存在: articleId={}", articleId);
                continue;
            }

            // 4. 比较Redis和MySQL的值，不同才更新
            Integer mysqlViews = article.getViews();
            if (mysqlViews != null && mysqlViews == redisCount) {
                log.debug("阅读数据已一致，跳过: articleId={}, views={}", articleId, redisCount);
                continue;
            }

            // 5. 更新MySQL的views字段
            LambdaUpdateWrapper<ApArticle> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ApArticle::getId, articleId);
            updateWrapper.set(ApArticle::getViews, redisCount);
            apArticleMapper.update(null, updateWrapper);

            syncCount++;
            log.info("同步阅读数据成功: articleId={}, redis={}, mysql={}", articleId, redisCount, mysqlViews);
        }

        log.info("Redis阅读数据同步完成，共同步{}条记录", syncCount);
    }
}
