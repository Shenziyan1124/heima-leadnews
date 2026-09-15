-- 文章服务数据库初始化脚本
CREATE DATABASE IF NOT EXISTS leadnews_article DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE leadnews_article;

-- 文章信息表
CREATE TABLE IF NOT EXISTS `ap_article` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `author_id` int(11) UNSIGNED NULL DEFAULT NULL COMMENT '文章作者的ID',
  `author_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者昵称',
  `channel_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '文章所属频道ID',
  `channel_name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '频道名称',
  `layout` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '文章布局 0 无图文章 1 单图文章 2 多图文章',
  `flag` tinyint(3) UNSIGNED NULL DEFAULT NULL COMMENT '文章标记 0 普通文章 1 热点文章 2 置顶文章 3 精品文章 4 大V文章',
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章图片 多张逗号分隔',
  `labels` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章标签最多3个 逗号分隔',
  `likes` int(5) UNSIGNED NULL DEFAULT NULL COMMENT '点赞数量',
  `collection` int(5) UNSIGNED NULL DEFAULT NULL COMMENT '收藏数量',
  `comment` int(5) UNSIGNED NULL DEFAULT NULL COMMENT '评论数量',
  `views` int(5) UNSIGNED NULL DEFAULT NULL COMMENT '阅读数量',
  `province_id` int(11) UNSIGNED NULL DEFAULT NULL COMMENT '省市',
  `city_id` int(11) UNSIGNED NULL DEFAULT NULL COMMENT '市区',
  `county_id` int(11) UNSIGNED NULL DEFAULT NULL COMMENT '区县',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `publish_time` datetime(0) NULL DEFAULT NULL COMMENT '发布时间',
  `sync_status` tinyint(1) NULL DEFAULT 0 COMMENT '同步状态',
  `origin` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '来源',
  `static_url` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章信息表，存储已发布的文章' ROW_FORMAT = DYNAMIC;

-- 文章配置表
CREATE TABLE IF NOT EXISTS `ap_article_config` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `article_id` bigint(20) NULL DEFAULT NULL COMMENT '文章ID',
  `is_comment` tinyint(1) NULL DEFAULT NULL COMMENT '是否可评论 1 true 0 false',
  `is_forward` tinyint(1) NULL DEFAULT NULL COMMENT '是否可转发 1 true 0 false',
  `is_like` tinyint(1) NULL DEFAULT NULL COMMENT '是否点赞 1 true 0 false',
  `is_collect` tinyint(1) NULL DEFAULT NULL COMMENT '是否收藏 1 true 0 false',
  `is_delete` tinyint(1) NULL DEFAULT NULL COMMENT '是否删除 1 true 0 false',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'APP文章配置表' ROW_FORMAT = DYNAMIC;

-- 文章黑白名单表
CREATE TABLE IF NOT EXISTS `ap_article_portrait` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `article_id` bigint(20) NULL DEFAULT NULL COMMENT '文章ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `is_like` tinyint(1) NULL DEFAULT NULL COMMENT '是否点赞 1 true 0 false',
  `is_collection` tinyint(1) NULL DEFAULT NULL COMMENT '是否收藏 1 true 0 false',
  `is_read` tinyint(1) NULL DEFAULT NULL COMMENT '是否阅读 1 true 0 false',
  `is_cancel_like` tinyint(1) NULL DEFAULT NULL COMMENT '是否取消点赞 1 true 0 false',
  `is_cancel_collection` tinyint(1) NULL DEFAULT NULL COMMENT '是否取消收藏 1 true 0 false',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'APP文章喜好 portrait' ROW_FORMAT = DYNAMIC;

-- 文章阅读记录表
CREATE TABLE IF NOT EXISTS `ap_read_history` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `article_id` bigint(20) NULL DEFAULT NULL COMMENT '文章ID',
  `author_id` bigint(20) NULL DEFAULT NULL COMMENT '作者ID',
  `read_time` datetime(0) NULL DEFAULT NULL COMMENT '阅读时间',
  `see_history` tinyint(1) NULL DEFAULT 1 COMMENT '是否可见 1 true 0 false',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'APP阅读历史表' ROW_FORMAT = DYNAMIC;

-- 文章收藏表
CREATE TABLE IF NOT EXISTS `ap_collection` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `collection_time` datetime(0) NULL DEFAULT NULL COMMENT '收藏时间',
  `article_id` bigint(20) NULL DEFAULT NULL COMMENT '文章ID',
  `is_cancel` tinyint(1) NULL DEFAULT 0 COMMENT '是否取消 1 true 0 false',
  `cancel_time` datetime(0) NULL DEFAULT NULL COMMENT '取消收藏时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'APP文章收藏表' ROW_FORMAT = DYNAMIC;

-- 文章点赞表
CREATE TABLE IF NOT EXISTS `ap_likes` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `content_id` bigint(20) NULL DEFAULT NULL COMMENT '文章ID',
  `like_time` datetime(0) NULL DEFAULT NULL COMMENT '点赞时间',
  `is_cancel` tinyint(1) NULL DEFAULT 0 COMMENT '是否取消 1 true 0 false',
  `cancel_time` datetime(0) NULL DEFAULT NULL COMMENT '取消时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'APP点赞表' ROW_FORMAT = DYNAMIC;

-- 文章统计表
CREATE TABLE IF NOT EXISTS `ap_article_statistics` (
  `id` bigint(20) NOT NULL,
  `article_id` bigint(20) NULL DEFAULT NULL COMMENT '文章ID',
  `page_views` int(11) NULL DEFAULT 0 COMMENT '阅读量',
  `comment` int(11) NULL DEFAULT 0 COMMENT '评论量',
  `likes` int(11) NULL DEFAULT 0 COMMENT '点赞量',
  `collection` int(11) NULL DEFAULT 0 COMMENT '收藏量',
  `forward` int(11) NULL DEFAULT 0 COMMENT '转发量',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'APP文章统计表' ROW_FORMAT = DYNAMIC;
