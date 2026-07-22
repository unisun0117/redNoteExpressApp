-- ============================================================
-- 菜单管理 + 字典管理
-- menu：菜单表（目录/菜单/按钮三级）
-- role_menu：角色-菜单关联
-- dict：字典类型
-- dict_item：字典项
-- ============================================================

-- 菜单表
CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `code` VARCHAR(60) NOT NULL COMMENT '编码，全局唯一',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID，0表示根节点',
  `name` VARCHAR(60) NOT NULL COMMENT '菜单名称',
  `type` ENUM('MENU', 'PAGE', 'BUTTON') NOT NULL COMMENT '菜单类型',
  `menu_group` ENUM('WEB','WECOM') NOT NULL DEFAULT 'WEB' COMMENT '菜单组',
  `path` VARCHAR(255) COMMENT '路由路径',
  `component` VARCHAR(200) COMMENT '前端组件路径',
  `icon` VARCHAR(255) COMMENT '图标',
  `sort` INT DEFAULT 0 COMMENT '排序号',
  `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(64) COMMENT '修改人',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_code (code),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 按钮权限关联表
CREATE TABLE IF NOT EXISTS sys_menu_permission (
    menu_id BIGINT NOT NULL COMMENT '按钮ID（type=BUTTON）',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限码（Permissions 枚举值）',
    created_by VARCHAR(64) COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (menu_id, permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='按钮权限关联表';

-- 角色-菜单关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`role_id`, `menu_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 字典类型表
CREATE TABLE IF NOT EXISTS `sys_dict` (
  `code` VARCHAR(60) NOT NULL COMMENT '字典编码',
  `name` VARCHAR(100) NOT NULL COMMENT '字典名称',
  `description` VARCHAR(255) COMMENT '描述',
  `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(64) COMMENT '修改人',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- 字典项表
CREATE TABLE IF NOT EXISTS `sys_dict_item` (
  `dict_code` VARCHAR(60) NOT NULL COMMENT '字典编码',
  `value` VARCHAR(60) NOT NULL COMMENT '数据值',
  `label` VARCHAR(100) NOT NULL COMMENT '显示文本',
  `sort` INT DEFAULT 0 COMMENT '排序号',
  `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
  `created_by` VARCHAR(64) COMMENT '创建人',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` VARCHAR(64) COMMENT '修改人',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`dict_code`, `value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典项表';
