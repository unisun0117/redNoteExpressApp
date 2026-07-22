CREATE TABLE `prd_category` (
    `id` VARCHAR(6) NOT NULL COMMENT '分类编号',
    `name` VARCHAR(32) NOT NULL COMMENT '分类名称',
    `alias` VARCHAR(32) DEFAULT NULL COMMENT '自定义-别名',
    `parent_id` VARCHAR(6) NOT NULL DEFAULT '0' COMMENT '上级分类编号(0表示顶级分类)',
    `parent_id_path` VARCHAR(500) DEFAULT NULL COMMENT '上级分类编号路径(格式: /1/2/3/)',
    `parent_name_path` VARCHAR(500) DEFAULT NULL COMMENT '上级分类名称路径(格式: /蔬菜类/根茎类/萝卜类/)',
    `level` TINYINT NOT NULL DEFAULT 0 COMMENT '层级(0大分类,1中分类,2小分类)',
    sort            int                         default 0                 null comment '排序号',
    status          enum ('ACTIVE', 'INACTIVE') default 'ACTIVE'          null comment '状态',
    `memo` VARCHAR(128) DEFAULT NULL COMMENT '备注',
    created_at      timestamp                   default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at      timestamp                   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';