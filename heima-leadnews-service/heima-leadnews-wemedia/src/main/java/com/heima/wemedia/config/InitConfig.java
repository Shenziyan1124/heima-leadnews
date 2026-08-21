package com.heima.wemedia.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.heima.apis.article.fallback")
/**
 *  fallback 的 Bean 必须注册在「调用方」(wemedia)的 Spring 容器里,而它的包不在 wemedia的扫描范围内。
 *  所以需要在 wemedia 模块下创建一个配置类,手动将 fallback 的 Bean 注册到 wemediaSpring 容器里。
 */
public class InitConfig {
}
