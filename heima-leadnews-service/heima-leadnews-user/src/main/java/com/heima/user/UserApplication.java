package com.heima.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication // 标记这是一个Spring Boot应用
@EnableDiscoveryClient // 启用服务发现
@MapperScan("com.heima.user.mapper") // 指定Mapper接口的包路径
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args); // 启动类
    }
}
