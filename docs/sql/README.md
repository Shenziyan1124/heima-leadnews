# 数据库初始化脚本

## 目录结构

```
docs/sql/
├── README.md           # 本文件
├── leadnews_user.sql   # 用户服务数据库
├── leadnews_article.sql # 文章服务数据库
├── leadnews_wemedia.sql # 自媒体服务数据库
├── leadnews_schedule.sql # 定时任务数据库
└── leadnews_comment.sql  # 评论服务MongoDB脚本
```

## 使用说明

### MySQL 数据库

按顺序执行以下脚本：

```bash
# 1. 用户服务
mysql -u root -p < docs/sql/leadnews_user.sql

# 2. 文章服务
mysql -u root -p < docs/sql/leadnews_article.sql

# 3. 自媒体服务
mysql -u root -p < docs/sql/leadnews_wemedia.sql

# 4. 定时任务服务
mysql -u root -p < docs/sql/leadnews_schedule.sql
```

### MongoDB 数据库

```bash
# 连接MongoDB并执行脚本
mongo < docs/sql/leadnews_comment.sql
```

## 注意事项

1. 执行脚本前请确保已安装 MySQL 5.7+ 和 MongoDB 4.x+
2. 脚本会自动创建数据库，如已存在则不会覆盖
3. 只包含表结构，不含测试数据
4. 生产环境请根据实际需求调整字段默认值和索引
