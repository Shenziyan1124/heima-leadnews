package com.heima.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = "com.heima")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.heima.apis")
@MapperScan("com.heima.user.mapper")
@EnableKafka

public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args); // 启动类
    }
}
