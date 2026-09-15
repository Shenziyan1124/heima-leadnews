package com.heima.wemedia.service;

import com.heima.wemedia.WemediaApplication;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@Disabled("需要启动Nacos、数据库、Seata才能运行")
@SpringBootTest(classes = WemediaApplication.class)
class WmNewsAutoScanServiceTest {

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Test
    void autoScanWmNews() {
        wmNewsAutoScanService.autoScanWmNews(6250);
    }
}