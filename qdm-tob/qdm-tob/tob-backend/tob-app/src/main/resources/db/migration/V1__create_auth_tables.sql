-- 小程序用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `mobile` VARCHAR(20) NOT NULL COMMENT '手机号',
    `real_name` VARCHAR(60) COMMENT '姓名',
    `wechat_openid` VARCHAR(60) COMMENT '微信 openid',
    `wechat_id` VARCHAR(60) COMMENT '微信账号',
    `wechat_nickname` VARCHAR(60) COMMENT '微信昵称',
    `wechat_avatar` VARCHAR(500) COMMENT '微信头像',
    `status` ENUM('ACTIVE', 'INACTIVE', 'FROZEN') DEFAULT 'ACTIVE' COMMENT '用户状态',
    `registered_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_at` TIMESTAMP NULL COMMENT '最后登录时间',
    `source` ENUM('WECHAT', 'ADMIN') DEFAULT 'WECHAT' COMMENT '用户来源',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_mobile` (`mobile`),
    UNIQUE KEY `uk_wechat_openid` (`wechat_openid`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序用户表';

-- 后台运营人员表
CREATE TABLE IF NOT EXISTS `sys_operator` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `mobile` VARCHAR(20) NOT NULL COMMENT '手机号',
    `employee_code` VARCHAR(60) COMMENT '工号（CAS:employeeCode）',
    `login_id` VARCHAR(60) COMMENT '登录账号（CAS:namePinyin）',
    `real_name` VARCHAR(60) COMMENT '姓名',
    `email` VARCHAR(60) COMMENT '邮箱',
    `type` ENUM('ADMIN', 'SALESMAN') NOT NULL COMMENT '用户类型',
    `status` ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE' COMMENT '用户状态',
    `last_login_at` TIMESTAMP NULL COMMENT '最后登录时间',
    `created_by` VARCHAR(64) COMMENT '创建人',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(64) COMMENT '修改人',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_mobile` (`mobile`),
    UNIQUE KEY `uk_employee_code` (`employee_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台运营人员表';
