# heima-leadnews

黑马头条 - 微服务架构新闻资讯平台

## 项目简介

基于 Spring Cloud + MyBatis-Plus + MongoDB + Elasticsearch 的微服务新闻资讯平台，包含用户端（APP）和自媒体管理端。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.3.9 | 基础框架 |
| Spring Cloud | - | 微服务框架 |
| MyBatis-Plus | 3.4.1 | ORM 框架 |
| MongoDB | - | 文档数据库（评论） |
| Elasticsearch | - | 搜索引擎 |
| Nacos | - | 注册中心/配置中心 |
| Gateway | - | 网关服务 |
| Sentinel | - | 限流熔断 |

## 项目结构

```
heima-leadnews/
├── heima-leadnews-common/        # 公共模块
├── heima-leadnews-model/         # 实体类模块
├── heima-leadnews-utils/         # 工具类模块
├── heima-leadnews-feign-api/     # Feign 接口定义
├── heima-leadnews-basic/         # 基础服务（文件上传等）
├── heima-leadnews-gateway/       # 网关服务
├── heima-leadnews-service/       # 业务服务
│   ├── heima-leadnews-user/      # 用户服务 (51803)
│   ├── heima-leadnews-article/   # 文章服务 (51804)
│   ├── heima-leadnews-wemedia/   # 自媒体服务 (51806)
│   ├── heima-leadnews-comment/   # 评论服务 (51807)
│   ├── heima-leadnews-behavior/  # 行为服务 (51808)
│   ├── heima-leadnews-search/    # 搜索服务 (51809)
│   ├── heima-leadnews-schedule/  # 定时任务 (51810)
│   └── heima-leadnews-admin/     # 管理后台 (51811)
└── heima-leadnews-test/          # 测试模块
```

## 服务端口

| 服务 | 端口 |
|------|------|
| Gateway | 51800 |
| Nacos | 8848 |
| User | 51803 |
| Article | 51804 |
| Wemedia | 51806 |
| Comment | 51807 |
| Behavior | 51808 |
| Search | 51809 |
| Schedule | 51810 |
| Admin | 51811 |

## 快速开始

### 环境要求

- JDK 1.8
- Maven 3.6+
- MySQL 5.7+
- MongoDB 4.x+
- Nacos 2.x+
- Elasticsearch 7.x+

### 编译项目

```bash
# 设置 JDK 1.8
$env:JAVA_HOME="C:\Program Files\Java\latest\jdk-1.8"

# 编译所有模块
mvn clean install -DskipTests

# 编译单个服务
mvn install -pl heima-leadnews-service/heima-leadnews-article -am -DskipTests
```

### 启动服务

1. 启动基础设施：MySQL、MongoDB、Nacos、Elasticsearch
2. 按顺序启动服务：User → Article → Wemedia → Comment → Behavior → Gateway

## 核心功能

### 用户端 (APP)
- 文章列表/详情/搜索
- 文章评论/回复/点赞
- 用户关注/粉丝
- 文章收藏/阅读历史

### 自媒体端
- 文章发布/管理
- 评论管理（列表/状态开关/删除）
- 数据统计（阅读/点赞/评论/收藏）
- 文章排序（按时间/点赞/阅读/评论/收藏）

## API 文档

启动服务后访问 Knife4j 文档：
- User: http://localhost:51803/doc.html
- Article: http://localhost:51804/doc.html
- Wemedia: http://localhost:51806/doc.html
- Comment: http://localhost:51807/doc.html

## 开发说明

### Feign 调用规范
- 接口定义在 `heima-leadnews-feign-api`
- 实现类用 `@RestController` 注解
- Fallback 类实现 `FallbackFactory` 接口

### MongoDB 使用
- 评论存储在 `ap_comment` 集合
- 回复存储在 `ap_comment_reply` 集合
- 使用 `MongoTemplate` 进行原子操作

### 分页查询
- 使用 `PageRequestDto` 基类
- 返回 `PageResponseResult` 对象
- `checkParam()` 自动修正非法参数
