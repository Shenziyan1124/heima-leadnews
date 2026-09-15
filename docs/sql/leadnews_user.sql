-- 用户服务数据库初始化脚本
CREATE DATABASE IF NOT EXISTS leadnews_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE leadnews_user;

-- APP用户信息表
CREATE TABLE IF NOT EXISTS `ap_user` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `salt` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码、通信等加密盐',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码,md5加密',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `sex` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '0 男 1 女 2 未知',
  `is_certification` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '0 未 1 是',
  `is_identity_authentication` tinyint(1) NULL DEFAULT NULL COMMENT '是否身份认证',
  `status` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '0正常 1锁定',
  `flag` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '0 普通用户 1 自媒体人 2 大V',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '注册时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'APP用户信息表' ROW_FORMAT = DYNAMIC;

-- APP用户粉丝信息表
CREATE TABLE IF NOT EXISTS `ap_user_fan` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) UNSIGNED NULL DEFAULT NULL COMMENT '用户ID',
  `fans_id` int(11) UNSIGNED NULL DEFAULT NULL COMMENT '粉丝ID',
  `fans_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '粉丝昵称',
  `level` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '粉丝忠实度 0 正常 1 潜力股 2 勇士 3 铁杆 4 老铁',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `is_display` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '是否可见我动态',
  `is_shield_letter` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '是否屏蔽私信',
  `is_shield_comment` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '是否屏蔽评论',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'APP用户粉丝信息表' ROW_FORMAT = DYNAMIC;

-- APP用户关注信息表
CREATE TABLE IF NOT EXISTS `ap_user_follow` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) UNSIGNED NULL DEFAULT NULL COMMENT '用户ID',
  `follow_id` int(11) UNSIGNED NULL DEFAULT NULL COMMENT '关注作者ID',
  `follow_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '粉丝昵称',
  `level` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '关注度 0 偶尔感兴趣 1 一般 2 经常 3 高度',
  `is_notice` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '是否动态通知',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'APP用户关注信息表' ROW_FORMAT = DYNAMIC;

-- APP实名认证信息表
CREATE TABLE IF NOT EXISTS `ap_user_realname` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) UNSIGNED NULL DEFAULT NULL COMMENT '账号ID',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名称',
  `idno` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '资源名称',
  `font_image` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '正面照片',
  `back_image` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '背面照片',
  `hold_image` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手持照片',
  `live_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活体照片',
  `status` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '状态 0 创建中 1 待审核 2 审核失败 9 审核通过',
  `reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `submited_time` datetime(0) NULL DEFAULT NULL COMMENT '提交时间',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'APP实名认证信息表' ROW_FORMAT = DYNAMIC;
