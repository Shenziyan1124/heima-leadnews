package com.heima.wemedia;

import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.file.service.FileStorageService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

@Ignore("需要启动数据库和阿里云服务才能运行")
@SpringBootTest(classes = com.heima.wemedia.WemediaApplication.class)
@RunWith(SpringRunner.class) // 指定JUnit测试 runner
public class AliyunTest {

    @Autowired
    private GreenTextScan greenTextScan;

    @Autowired
    private GreenImageScan greenImageScan;

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void testGreenTextScan() throws Exception {
        String content = "测试内容";
        System.out.println(greenTextScan.greeTextScan(content));
    }

    @Test
    public void testGreenImageScan() throws Exception {
        String path = "http://192.168.200.130:9000/leadnews/2026/08/20/8472088f097640e8876da656fb8a2a26.jpg";
        byte[] bytes = fileStorageService.downLoadFile(path);

        ArrayList<byte[]> list = new ArrayList<>();
        list.add(bytes);

        System.out.println(
                greenImageScan.imageScan(list)
        );
    }

}
