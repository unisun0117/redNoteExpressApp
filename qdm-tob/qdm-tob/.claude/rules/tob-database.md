---
description: 数据库设计规范
paths:
  - "docs/database/**/*"
  - "tob-backend/**/*.sql"
  - "tob-backend/**/db/migration/**"
  - "tob-backend/**/domain/**"
tags:
  - database
  - flyway
  - schema
---

## 数据库设计规范

### 表设计规范
- **表前缀**：每个模块使用统一前缀：`sys_`（系统）、`cst_`（客户）、`ord_`（订单）、`prd_`（商品）
- **表名**：使用小写字母 + 下划线命名，如 `ord_sales_order`
- **表注释**：所有表必须添加 COMMENT 注释说明表用途
- **引擎**：统一使用 InnoDB 引擎
- **字符集**：统一使用 utf8mb4 字符集

### 字段设计规范
- **创建时间**：统一命名为 `created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'`
- **更新时间**：统一命名为 `updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'`
- **创建人**：统一命名为 `created_by VARCHAR(50) NULL COMMENT '创建人'`
- **更新人**：统一命名为 `updated_by VARCHAR(50) NULL COMMENT '修改人'`
- **记录状态**：统一命名为 `status ENUM ('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态'`
- **枚举字段**：枚举类型字段必须使用 MySQL `ENUM` 类型
- **字段注释**：所有字段必须添加 COMMENT 注释，说明字段用途
- **字段命名**：使用小写字母 + 下划线，见名知意，避免缩写
- **NULL约束**：业务必填字段必须设为 `NOT NULL`，有默认值的字段设置合理默认值

### 金额/数量字段规范
- **金额字段**：统一使用 `DECIMAL(12,2)` 类型，单位为元，精确到分
- **数量字段**：统一使用 `DECIMAL(10,2)` 类型，支持小数（如 0.5 公斤）
- **比例/折扣**：使用 `DECIMAL(5,2)` 或 `DECIMAL(10,4)` 根据精度需求

### 索引设计规范
- **主键索引**：自增 id 自动为主键索引
- **唯一索引**：业务唯一键使用 `UNIQUE KEY uk_xxx` 命名
- **普通索引**：高频查询字段建立索引，命名为 `idx_xxx`
- **联合索引**：遵循最左前缀原则，命名为 `idx_xxx_yyy`
- **索引字段**：区分度低的字段（如性别、状态只有几个取值）不单独建索引

### 数据快照规范
- **订单类数据**：客户信息、商品信息、价格、仓库等关联数据在创建订单时必须进行快照存储
- **快照字段**：快照字段命名清晰，如 `customer_name`（客户名称快照）、`product_name`（商品名称快照）
- **快照原因**：快照数据确保历史订单不受后续基础数据变更影响

### 逻辑删除规范
- **禁止场景**：订单、支付、财务等具有审计属性的数据**禁止使用逻辑删除**
- **适用场景**：配置类、草稿类等数据可以使用逻辑删除
- **逻辑删除字段**：如使用逻辑删除，统一使用 `is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0=否，1=是'`

### 跨模块关联规范
- **禁止JOIN**：不同模块前缀的表**禁止JOIN查询**
- **冗余设计**：为了性能可合理冗余字段，如 `sales_region_name`、`warehouse_name`

### Flyway 迁移规范
- **版本号**：使用 `V{版本号}__{描述}.sql` 格式，如 `V11__create_sales_order_tables.sql`
- **描述命名**：使用小写字母 + 下划线，清晰描述变更内容
- **幂等性**：DDL 语句使用 `IF NOT EXISTS`、`IF EXISTS` 等保证可重复执行
- **事务安全**：批量数据迁移注意事务大小，避免长事务
- **回滚预案**：重要数据变更前考虑回滚方案

### 数据库命名示例
```sql
-- 好的示例
CREATE TABLE ord_sales_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    order_status ENUM('PENDING_PAYMENT','PENDING_OUTBOUND','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态',
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户名称（快照）',
    created_by VARCHAR(50) NULL COMMENT '创建人',
    updated_by VARCHAR(50) NULL COMMENT '修改人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单主表';
```
