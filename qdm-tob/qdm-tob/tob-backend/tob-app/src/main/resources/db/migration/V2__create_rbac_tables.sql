-- ============================================================
-- RBAC 权限模型（全动态授权）
-- permission：权限定义
-- api_permission：API ↔ 权限映射（动态授权核心）
-- role：角色定义
-- role_permission：角色-权限关系
-- admin_role：管理员-角色关系
-- ============================================================

-- 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `code` VARCHAR(100) NOT NULL COMMENT '权限码，如 order:view',
  `name` VARCHAR(100) NOT NULL COMMENT '权限显示名，如 查看订单',
  `description` VARCHAR(255) COMMENT '权限描述',
  `resource` VARCHAR(100) COMMENT '资源，如 order',
  `action` VARCHAR(60) COMMENT '操作，如 view/edit/delete',
  `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_resource` (`resource`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- API ↔ 权限映射表（全动态授权核心）
CREATE TABLE IF NOT EXISTS `sys_api_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '映射ID',
  `http_method` VARCHAR(10) NOT NULL COMMENT 'HTTP方法 GET/POST/PUT/DELETE',
  `url_pattern` VARCHAR(255) NOT NULL COMMENT 'URL匹配模式，Ant风格，如 /api/order/**',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '所需权限码，如 order:view',
  `description` VARCHAR(255) COMMENT '描述',
  `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API权限映射表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `code` VARCHAR(60) NOT NULL COMMENT '角色码，如 ADMIN/SALESMAN',
  `name` VARCHAR(60) NOT NULL COMMENT '角色显示名，如 管理员',
  `description` VARCHAR(255) COMMENT '角色描述',
  `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(64) COMMENT '修改人',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 角色-权限关系表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关系表';

-- 运营/管理员-角色关系表
CREATE TABLE IF NOT EXISTS `sys_operator_role` (
  `operator_id` BIGINT NOT NULL COMMENT '运营/管理员ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`operator_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运营/管理员角色关系表';
