-- 定时任务服务数据库初始化脚本
CREATE DATABASE IF NOT EXISTS leadnews_schedule DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE leadnews_schedule;

-- 任务信息表
CREATE TABLE IF NOT EXISTS `taskinfo` (
  `task_id` bigint(20) NOT NULL COMMENT '任务id',
  `execute_time` datetime(3) NOT NULL COMMENT '执行时间',
  `parameters` longblob NULL COMMENT '参数',
  `priority` int(11) NOT NULL COMMENT '优先级',
  `task_type` int(11) NOT NULL COMMENT '任务类型',
  PRIMARY KEY (`task_id`) USING BTREE,
  INDEX `index_taskinfo_time`(`task_type`, `priority`, `execute_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- 任务日志表
CREATE TABLE IF NOT EXISTS `taskinfo_logs` (
  `task_id` bigint(20) NOT NULL COMMENT '任务id',
  `execute_time` datetime(3) NOT NULL COMMENT '执行时间',
  `parameters` longblob NULL COMMENT '参数',
  `priority` int(11) NOT NULL COMMENT '优先级',
  `task_type` int(11) NOT NULL COMMENT '任务类型',
  `version` int(11) NOT NULL COMMENT '版本号,用乐观锁',
  `status` int(11) NULL DEFAULT 0 COMMENT '状态 0=初始化状态 1=EXECUTED 2=CANCELLED',
  PRIMARY KEY (`task_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
