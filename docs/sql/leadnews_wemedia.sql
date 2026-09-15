-- 自媒体服务数据库初始化脚本
CREATE DATABASE IF NOT EXISTS leadnews_wemedia DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE leadnews_wemedia;

-- 频道信息表
CREATE TABLE IF NOT EXISTS `wm_channel` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '频道名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '频道描述',
  `is_default` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '是否默认频道',
  `status` tinyint(1) UNSIGNED NULL DEFAULT NULL,
  `ord` tinyint(3) UNSIGNED NULL DEFAULT NULL COMMENT '默认排序',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '频道信息表' ROW_FORMAT = DYNAMIC;

-- 默认频道数据
INSERT INTO `wm_channel` VALUES (0, '其他', '其他', 1, 1, 12, NOW());
INSERT INTO `wm_channel` VALUES (1, 'java', '后端框架', 1, 1, 1, NOW());
INSERT INTO `wm_channel` VALUES (2, 'Mysql', '轻量级数据库', 1, 1, 4, NOW());
INSERT INTO `wm_channel` VALUES (3, 'Vue', '阿里前端框架', 1, 1, 5, NOW());
INSERT INTO `wm_channel` VALUES (4, 'Python', '未来的语言', 1, 1, 6, NOW());
INSERT INTO `wm_channel` VALUES (5, 'Weex', '向未来致敬', 1, 1, 7, NOW());
INSERT INTO `wm_channel` VALUES (6, '大数据', '大数据', 1, 1, 10, NOW());

-- 自媒体用户表
CREATE TABLE IF NOT EXISTS `wm_user` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `ap_user_id` int(11) UNSIGNED NULL DEFAULT NULL COMMENT 'APP用户ID',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `salt` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '盐',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` int(1) UNSIGNED NULL DEFAULT NULL COMMENT '状态 0 正常 1 被禁用',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_id`(`ap_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '自媒体用户表' ROW_FORMAT = DYNAMIC;

-- 自媒体文章表
CREATE TABLE IF NOT EXISTS `wm_news` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` int(11) UNSIGNED NULL DEFAULT NULL COMMENT '自媒体用户ID',
  `author_id` bigint(20) NULL DEFAULT NULL COMMENT '文章作者ID',
  `author_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者笔名',
  `channel_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '频道ID',
  `channel_name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '频道名称',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `type` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '文章类型 0 图文 1 图集 2 视频',
  `cover` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面',
  `publish_time` datetime(0) NULL DEFAULT NULL COMMENT '发布时间',
  `status` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '文章状态 0 草稿 1 待审核 2 审核失败 3 审核成功 4 已发布',
  `reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核失败原因',
  `article_id` bigint(20) NULL DEFAULT NULL COMMENT '文章ID',
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '自媒体文章表' ROW_FORMAT = DYNAMIC;

-- 自媒体素材信息表
CREATE TABLE IF NOT EXISTS `wm_material` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) UNSIGNED NULL DEFAULT NULL COMMENT '自媒体用户ID',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片地址',
  `type` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '素材类型 0 图片 1 视频',
  `is_collection` tinyint(1) NULL DEFAULT NULL COMMENT '是否收藏',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '自媒体图文素材信息表' ROW_FORMAT = DYNAMIC;

-- 自媒体粉丝数据统计表
CREATE TABLE IF NOT EXISTS `wm_fans_statistics` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) UNSIGNED NULL DEFAULT NULL COMMENT '主账号ID',
  `article` int(11) UNSIGNED NULL DEFAULT NULL COMMENT '子账号ID',
  `read_count` int(11) UNSIGNED NULL DEFAULT NULL,
  `comment` int(11) UNSIGNED NULL DEFAULT NULL,
  `follow` int(11) UNSIGNED NULL DEFAULT NULL,
  `collection` int(11) UNSIGNED NULL DEFAULT NULL,
  `forward` int(11) UNSIGNED NULL DEFAULT NULL,
  `likes` int(11) UNSIGNED NULL DEFAULT NULL,
  `unlikes` int(11) UNSIGNED NULL DEFAULT NULL,
  `unfollow` int(11) UNSIGNED NULL DEFAULT NULL,
  `burst` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_time` date NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_id_time`(`user_id`, `created_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '自媒体粉丝数据统计表' ROW_FORMAT = DYNAMIC;

-- 新闻统计表
CREATE TABLE IF NOT EXISTS `wm_news_statistics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `article_id` bigint(20) NULL DEFAULT NULL COMMENT '文章ID',
  `read_count` int(11) NULL DEFAULT 0 COMMENT '阅读量',
  `comment_count` int(11) NULL DEFAULT 0 COMMENT '评论量',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞量',
  `collect_count` int(11) NULL DEFAULT 0 COMMENT '收藏量',
  `forward_count` int(11) NULL DEFAULT 0 COMMENT '转发量',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '新闻统计表' ROW_FORMAT = DYNAMIC;
