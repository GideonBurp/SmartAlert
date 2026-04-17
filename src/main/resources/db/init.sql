-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_alert DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE smart_alert;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) DEFAULT '' COMMENT '密码（验证码登录为空）',
    `telephone` VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `gender` VARCHAR(10) DEFAULT 'UNKNOWN' COMMENT '性别: MALE-男, FEMALE-女, UNKNOWN-未知',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `state` VARCHAR(20) NOT NULL DEFAULT 'INIT' COMMENT '状态: INIT-初始化, AUTH-已认证, FROZEN-冻结',
    `user_role` VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER' COMMENT '用户角色: CUSTOMER-普通用户, ADMIN-管理员',
    `invite_code` VARCHAR(20) DEFAULT NULL COMMENT '邀请码',
    `certification` TINYINT(1) DEFAULT 0 COMMENT '是否实名认证',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_telephone` (`telephone`),
    KEY `idx_invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入测试数据（管理员账号，需要密码）
INSERT INTO `user` (username, password, telephone, email, nickname, gender, state, user_role, invite_code, certification) 
VALUES ('admin', 'admin123', '13800138000', 'admin@example.com', '管理员', 'MALE', 'AUTH', 'ADMIN', 'ADMIN001', 1);

-- 定时推送任务表
CREATE TABLE IF NOT EXISTS `alert_task` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '创建用户ID',
    `theme` VARCHAR(100) NOT NULL COMMENT '主题',
    `recipient_name` VARCHAR(50) NOT NULL COMMENT '接收人姓名',
    `recipient_gender` VARCHAR(10) DEFAULT 'UNKNOWN' COMMENT '接收人性别: MALE-男, FEMALE-女, UNKNOWN-未知',
    `recipient_phone` VARCHAR(20) NOT NULL COMMENT '接收人手机号',
    `alert_type` VARCHAR(50) NOT NULL COMMENT '推送类型',
    `content` TEXT NOT NULL COMMENT '推送内容',
    `alert_time` DATETIME NOT NULL COMMENT '推送时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待推送, 1-已推送, 2-失败',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_alert_time (`alert_time`),
    INDEX idx_status (`status`),
    INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时推送任务表';

