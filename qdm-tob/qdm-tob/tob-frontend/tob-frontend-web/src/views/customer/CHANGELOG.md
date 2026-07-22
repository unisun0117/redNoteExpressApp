# 客户档案模块 (Customer Archive)

> **归属中心**：02-客户中心
> **模块路径**：`src/views/customer/customer-archive/`
> **后端 Controller**：`CustomerArchiveController` (`/api/admin/customer-archive`)
> **最后更新**：2026-07-13

---

## 模块概述

客户档案管理是 PC 后台端的核心模块，用于管理 B 端客户的 **公司收货地址档案**。支持管理员全量查看、筛选、新增、编辑、分配审核人、绑定小程序账号等操作。

## 功能列表

| 功能 | 说明 | 状态 |
|------|------|------|
| 列表查询与筛选 | 按公司名称、销售大区、所在地区、业务员、审核状态、审核人、注册时间筛选，分页 20 条/页 | ✅ |
| 新增客户档案 | 管理员直接录入，状态自动设为"已通过"（无需审核流程） | ✅ |
| 查看详情 | 弹窗/页面展示完整档案信息、审核日志、绑定用户 | ✅ |
| 编辑业务属性 | 仅已通过状态可编辑：收货人信息、价格组、结算公司、经营类型、结算类型、内部备注 | ✅ |
| 分配审核人 | 仅待审核状态可分配，审核人列表来自业务员推荐码管理 | ✅ |
| 绑定/解绑小程序账号 | 仅已通过状态可操作，搜索用户来自客户账号管理 | ✅ |

## 数据表结构

### 1. `cst_company_archive`（客户档案主表）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键，自增 |
| `sap_customer_code` | VARCHAR(30) | SAP 客户编码（审核通过后 SAP 回写） |
| `company_name` | VARCHAR(100) | 公司名称 |
| `license_no` | VARCHAR(50) | 营业执照编号 |
| `door_photo` | VARCHAR(500) | 门头照 URL |
| `license_photo` | VARCHAR(500) | 营业执照照 URL |
| `storage_photos` | TEXT | 存放位置照片（JSON 数组） |
| `contact_name` | VARCHAR(50) | 收货人姓名 |
| `contact_phone` | VARCHAR(20) | 收货人联系电话 |
| `province` | VARCHAR(50) | 所在省份 |
| `city` | VARCHAR(50) | 所在城市 |
| `district` | VARCHAR(50) | 所在区县 |
| `address` | VARCHAR(500) | 详细收货地址 |
| `longitude` | DECIMAL(10,7) | 经度 |
| `latitude` | DECIMAL(10,7) | 纬度 |
| `receive_time_start` | VARCHAR(5) | 可收货时段开始（HH:mm） |
| `receive_time_end` | VARCHAR(5) | 可收货时段结束（HH:mm） |
| `receive_requirement` | TEXT | 收货要求 |
| `sales_region_id` | BIGINT | 归属销售大区 ID |
| `sales_region_name` | VARCHAR(50) | 归属销售大区名称 |
| `salesman_id` | BIGINT | 归属业务员 ID |
| `salesman_name` | VARCHAR(50) | 归属业务员姓名 |
| `referral_code` | VARCHAR(10) | 提交时填写的业务员推荐码 |
| `audit_status` | VARCHAR(20) | 审核状态：PENDING/APPROVED/REJECTED |
| `price_group` | VARCHAR(50) | 价格组 |
| `settle_company` | VARCHAR(100) | 结算公司 |
| `business_type` | VARCHAR(50) | 经营类型 |
| `settle_type` | VARCHAR(20) | 结算类型：CASH/PERIOD |
| `internal_remark` | TEXT | 内部收货备注 |
| `auditor_id` | BIGINT | 审核处理人 ID |
| `auditor_name` | VARCHAR(50) | 审核处理人姓名 |
| `auditor_type` | VARCHAR(20) | 审核人类型：SALESMAN/MANAGER |
| `audit_reject_reason` | VARCHAR(500) | 审核驳回原因 |
| `audit_time` | DATETIME | 审核处理时间 |
| `submit_user_id` | BIGINT | 提交用户 ID |
| `submit_user_name` | VARCHAR(50) | 提交用户姓名 |
| `last_order_time` | DATETIME | 最近下单时间 |
| `created_at` | DATETIME | 创建/提交时间 |
| `updated_at` | DATETIME | 最近更新时间 |

### 2. `cst_archive_audit_log`（审核历史日志）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键，自增 |
| `archive_id` | BIGINT | 关联档案 ID |
| `action` | VARCHAR(20) | 操作动作：SUBMIT/APPROVE/REJECT/RE_SUBMIT/ASSIGN |
| `operator_id` | BIGINT | 操作人 ID |
| `operator_type` | VARCHAR(20) | 操作人类型：CUSTOMER/SALESMAN/MANAGER/ADMIN |
| `operator_name` | VARCHAR(50) | 操作人姓名 |
| `remark` | VARCHAR(500) | 备注/审批意见 |
| `created_at` | DATETIME | 操作时间 |

### 3. `cst_archive_user_binding`（用户绑定关系）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键，自增 |
| `archive_id` | BIGINT | 关联档案 ID |
| `user_id` | BIGINT | 小程序用户 ID |
| `user_name` | VARCHAR(50) | 用户姓名 |
| `user_mobile` | VARCHAR(20) | 用户手机号 |
| `member_role` | VARCHAR(20) | 成员角色：ADMIN/MEMBER |
| `invite_code` | VARCHAR(20) | 邀请码 |
| `invite_code_created_at` | DATETIME | 邀请码生成时间 |
| `binding_status` | VARCHAR(20) | 绑定状态：BOUND/UNBOUND |
| `created_at` | DATETIME | 绑定时间 |
| `updated_at` | DATETIME | 更新时间 |

### 4. `sys_salesman`（业务员推荐码，分配审核人数据源）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `user_id` | BIGINT | 关联后台用户 ID |
| `referral_code` | VARCHAR(10) | 推荐码（4 位数字字母） |
| `code_status` | VARCHAR(20) | 推荐码状态：VALID/EMPTY/INVALID |
| `created_by` | VARCHAR(50) | 创建人 |
| `updated_by` | VARCHAR(50) | 修改人 |
| `created_at` | DATETIME | 创建时间 |
| `updated_at` | DATETIME | 修改时间 |

### 5. `sys_user`（用户账号，绑定账号数据源）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `real_name` | VARCHAR(50) | 真实姓名 |
| `mobile` | VARCHAR(20) | 手机号 |
| `source` | VARCHAR(20) | 注册来源：WECHAT/ADMIN |
| `status` | VARCHAR(20) | 账号状态：ACTIVE/INACTIVE/FROZEN |
| `wechat_openid` | VARCHAR(100) | 微信 OpenID |
| `wechat_id` | VARCHAR(100) | 微信号 |
| `wechat_nickname` | VARCHAR(100) | 微信昵称 |
| `wechat_avatar` | VARCHAR(500) | 微信头像 URL |
| `registered_at` | DATETIME | 注册时间 |
| `last_login_at` | DATETIME | 最后登录时间 |

## API 接口列表

### 客户档案 CRUD（`/api/admin/customer-archive`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询客户档案列表（11 个筛选参数） |
| GET | `/detail` | 查询详情（含审核日志 + 绑定用户） |
| POST | `/create` | 后台新增（自动审核通过） |
| POST | `/edit` | 编辑业务属性 |
| POST | `/assign-auditor` | 手动分配审核人 |
| GET | `/bound-users` | 查询已绑定用户列表 |
| GET | `/search-users` | 搜索小程序用户（真实查询 sys_user） |
| POST | `/bind-user` | 绑定小程序用户 |
| POST | `/unbind-user` | 解绑小程序用户 |

### 外部依赖接口

| 方法 | 路径 | 用途 | 所属模块 |
|------|------|------|----------|
| GET | `/api/admin/operation/sales-region/list` | 销售大区下拉选项 | 运营管理 |
| GET | `/api/admin/salesman/referral/list` | 分配审核人下拉选项 | 业务员管理 |
| GET | `/api/admin/system/users/page` | 绑定账号搜索用户 | 客户账号管理 |

---

## 最近更新

### 2026-07-13

- **后端**：`create()` Web 端新增直接审核通过（状态 APPROVED，非 PENDING）
- **后端**：`submitAddress()` 小程序提交有推荐码时查找对应业务员设为审核人
- **后端**：`searchUsers()` 替换 mock 数据为 sys_user 真实查询
- **后端**：`CustomerArchiveCreateVO` 新增 `salesRegionName` 字段
- **前端-Web**：新增弹窗添加【销售大区】下拉选择字段
- **前端-Web**：分配审核人下拉改为动态加载 `/api/admin/salesman/referral/list`
- **前端-Web**：绑定账号用户搜索改为调用 `/api/admin/system/users/page`
