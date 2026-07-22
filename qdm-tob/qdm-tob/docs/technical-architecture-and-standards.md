---
name: tob-architecture-redesign
description: 钱鲜达 B2B 社区生鲜零售系统技术架构设计（单体架构、单库、完整模块设计）
metadata:
  type: project
---

# 钱鲜达 B2B 平台 - 技术架构与设计文档

> **项目名称：** 钱鲜达（QDM-TOB）社区生鲜零售 B2B 平台
> **生成日期：** 2026-06-30
> **适用范围：** 后端（tob-backend）、前端（tob-frontend）、产品规范

---

## 目录

1. [项目概览](#1-项目概览)
2. [技术栈总览](#2-技术栈总览)
3. [系统全景架构](#3-系统全景架构)
4. [后端架构设计](#4-后端架构设计)
5. [前端架构设计](#5-前端架构设计)
6. [数据库设计](#6-数据库设计)
7. [认证与授权体系](#7-认证与授权体系)
8. [API 设计规范](#8-api-设计规范)
9. [开发规范](#9-开发规范)
10. [部署与运维](#10-部署与运维)


## 1. 项目概览

### 1.1 业务定位

「钱鲜达」是面向社区生鲜零售的 B2B 智能管理平台，连接供应商、客户和物流仓储，提供从商品采购、订单管理、支付结算到物流配送的全链路数字化服务。

**核心指标：**
- 目标月营业额：10 亿
- 客单价：~1000 元
- 日均订单量：~350 万
- 开发团队：5 人

### 1.2 系统组成

```
qdm-tob/
├── tob-backend/           # 后端服务（Java 25 + Spring Boot 4.0.7）
├── tob-frontend/          # 前端 Monorepo（Vue 3）
│   ├── tob-frontend-web/      # PC 管理后台（Vue 3 + Element Plus）
│   ├── tob-frontend-mobile/   # 微信小程序（uni-app + uview-plus）
│   └── tob-frontend-h5/       # 企业微信 H5（Vue 3 + Vant）
├── product/               # 产品设计文档与原型
│   ├── prd/               # PRD 原型页面（HTML）
│   └── spec/              # 业务规格文档
└── docs/                  # 项目文档
```

### 1.3 用户角色

| 角色 | 访问终端 | 核心功能 |
|------|---------|---------|
| B 端客户 | 微信小程序 | 商品浏览、下单、订单查询、售后 |
| 运营管理员 | PC 管理后台 | 商品管理、订单处理、客户/客户审核 |
| 销售员 | PC 管理后台/企微 H5 | 客户审核、退款处理、快捷管理 |
| 系统管理员 | PC 管理后台 | 用户管理、权限配置、系统设置 |

---

## 2. 技术栈总览

### 2.1 后端技术栈

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **语言** | Java | 25 | LTS 长期支持版本 |
| **核心框架** | Spring Boot | 4.0.7 | 应用框架 |
| **模块化** | Spring Modulith | 2.1.0 | 单体模块化管理 |
| **安全** | Spring Security | 7.0+（Boot 4.0 内置） | 认证授权 |
| **ORM** | MyBatis Plus | 3.5.15 | 增强型 ORM |
| **数据库** | MySQL | 8.0+ | 阿里云 RDS，单实例单库 |
| **缓存** | Redis | 7.0+ | 阿里云 Redis + Redisson 4.6.1 |
| **本地缓存** | Caffeine | 3.2.4 | 高性能本地缓存 |
| **JWT** | JJWT | 0.13.0 | JSON Web Token |
| **API 文档** | SpringDoc + Scalar | 3.0.3 | OpenAPI 3.0 兼容 |
| **对象映射** | MapStruct | 1.6.3 | 编译期代码生成 |
| **Excel** | Apache Fesod | 2.0.2-incubating | Excel 导入导出 |
| **远程调用** | OpenFeign | Spring Cloud 内置 | 第三方 API 调用 |
| **构建工具** | Maven | 3.9+ | 多模块构建 |
| **Lombok** | Lombok | 1.18.46 | 样板代码消除 |

> **注意：** 当前版本号为 `1.0.0`，通过 `${revision}` 属性统一管理。

### 2.2 前端技术栈

| 类别 | 技术 | 版本 | 适用范围 |
|------|------|------|---------|
| **框架** | Vue 3 | ^3.4 - ^3.5 | 全部 |
| **语言** | TypeScript | ~5.4 - ~5.6 | 全部 |
| **构建** | Vite | ^5 - ^6 | 全部 |
| **CSS** | Tailwind CSS | ^3.4 | 全部 |
| **状态管理** | Pinia | ^2.x | 全部 |

**各端特有技术：**

| 端 | UI 框架 | 路由 | HTTP 客户端 | 特殊依赖 |
|----|---------|------|-------------|---------|
| PC Web | Element Plus ^2.9 | Vue Router ^4.5 | Axios ^1.7 | @element-plus/icons-vue |
| 微信小程序 | uview-plus ^3.3 | uni-app 内置 | luch-request ^3.1 | @dcloudio/uni-app 3.0 |
| 企业微信 H5 | Vant ^4.9 | Vue Router ^4.5 | Axios ^1.7 | weixin-js-sdk ^1.6 |

---

## 3. 系统全景架构

### 3.1 架构总览

```
                    ┌──────────────────────────────────────────────────┐
                    │                     前端 Monorepo                 │
                    ├──────────────────┬──────────────────┬────────────┤
                    │  tob-frontend-   │  tob-frontend-   │  tob-      │
                    │  web (PC 管理后台)│  mobile (小程序)  │  frontend- │
                    │  Vue 3 +         │  uni-app +       │  h5 (企微)  │
                    │  Element Plus    │  uview-plus      │  Vue 3 +   │
                    │  Port: 8080      │  WeChat MiniProg │  Vant      │
                    └────────┬─────────┴────────┬─────────┴─────┬──────┘
                             │                  │               │
                             └──────────────────┼───────────────┘
                                                │ HTTPS / CDN
                                        ┌───────┴───────┐
                                        │   tob-app     │
                                        │   Port: 8081  │
                                        │   Spring Boot 4.0 + Modulith
                                        │   SecurityConfiguration (JWT + 动态 RBAC)
                                        │   CORS 直接处理
                                        └───────┬───────┘
                                                │
                              ┌─────────────────┼─────────────────┐
                              │                 │                  │
                        ┌─────┴─────┐    ┌──────┴──────┐    ┌─────┴─────┐
                        │   MySQL   │    │    Redis    │    │  External │
                        │ 单库 qdm_tob│   │   (缓存)    │    │   APIs    │
                        │ sys_/cst_/ │    │             │    │ SAP/WMS/  │
                        │ ord_/prd_  │    │             │    │  企微/消息  │
                        └───────────┘    └─────────────┘    └───────────┘
```

### 3.2 架构决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 架构模式 | 单体 + Modulith | 5 人团队规模，避免微服务通讯成本；Modulith 保证模块边界，为未来拆分做准备 |
| 数据库 | 单实例单库 + 表前缀 | 简化部署运维，表前缀保留业务域识别能力 |
| 异步通讯 | Spring Event → RocketMQ | 单体期用进程内事件，微服务期无缝切换到消息队列 |
| 缓存策略 | Redis + Caffeine 两级 | Redis 集中缓存，Caffeine 本地缓存减少网络开销 |
| 认证 | JWT 无状态 | 无状态认证，无需 Session，SecurityContextHolder 获取用户信息 |

---

## 4. 后端架构设计

### 4.1 Maven 模块结构

```
tob-backend/
├── pom.xml                     # 父 POM，版本管理
├── tob-dependencies/           # BOM - 集中依赖版本管理
│   └── pom.xml
├── tob-app/                    # 核心业务应用
│   ├── pom.xml
│   └── src/main/
│       ├── java/cn/qdm/tob/
│       │   ├── TobApplication.java          # 主启动类
│       │   ├── infrastructure/               # 基础设施
│       │   │   ├── config/                   # SecurityConfiguration, Mybatis, Redis, Jackson
│       │   │   └── base/                     # TobBaseMapper, TobBaseService
│       │   └── modules/
│       │       ├── system/                   # 系统模块 ✅ 已实现
│       │       ├── customer/                 # 客户模块 📋 待实现
│       │       ├── order/                    # 订单模块 📋 待实现
│       │       └── product/                  # 商品模块 📋 待实现
│       └── resources/
│           ├── application.yml
│           ├── mapper/                       # MyBatis XML Mapper
│           └── db/migration/                 # Flyway 迁移脚本
├── tob-framework/               # 共享框架库
│   ├── pom.xml
│   └── src/main/java/cn/qdm/tob/framework/
│       ├── exception/           # 统一异常体系
│       ├── jackson/             # JSON 序列化扩展
│       ├── model/               # ResponseResult 等通用模型
│       ├── excel/               # Excel 导入导出
│       ├── description/         # 枚举描述机制
│       └── util/                # 工具类
└── tob-client/                  # 第三方客户端
    ├── pom.xml
    └── src/main/java/cn/qdm/tob/client/
        ├── sap/                 # SAP ERP 客户端
        ├── wms/                 # WMS 仓储客户端
        ├── wecom/               # 企业微信客户端
        └── message/             # 消息推送客户端
```

### 4.2 模块依赖关系

```
tob-dependencies (BOM)
       ↑ (版本管理)
┌──────┴──────────────────────┐
│      tob-framework          │ ← 共享库，被 tob-app、tob-client 依赖
│      tob-client             │ ← 第三方客户端，被 tob-app 依赖
└──────┬──────────────────────┘
       ↓
    tob-app ← 单一部署单元
```

**依赖规则：**
- `tob-framework` 仅依赖 Spring Boot Starter + 公共工具库，不依赖任何业务模块
- `tob-client` 依赖 `tob-framework`，不依赖 `tob-app`
- `tob-app` 依赖 `tob-framework` + `tob-client`
- 跨模块通讯通过 Spring Event（进程内异步）

### 4.3 业务模块设计（Spring Modulith）

每个业务模块使用 `@ApplicationModule(displayName = "...")` 声明，内部按子域划分。
所有子域遵循统一的分层结构：`api/`（Controller）、`dto/`（数据传输对象）、`domain/`（实体/枚举）、`service/`（业务逻辑）、`mapper/`（数据访问）。

**分层调用规则：**
- `api` → `service` → `mapper`（单向依赖）
- `api` 和 `service` 都可以依赖 `domain` 和 `dto`
- `domain` 不依赖任何其他层
- 跨模块通讯通过 Spring Event（进程内）

#### 4.3.1 系统模块（system）✅ 已实现

```
system/
├── SystemModule.java           # @ApplicationModule(displayName = "System Module")
├── auth/                        # 认证子域
│   ├── api/AuthController.java, admin/AdminAuthController.java, mall/WechatAuthController.java
│   ├── config/SecurityConfiguration.java            # Spring Security 配置
│   ├── security/JwtAuthenticationFilter.java        # JWT 过滤器
│   ├── security/DynamicAuthorizationManager.java    # 动态 RBAC 授权管理器
│   ├── domain/AuthType.java, UserPrincipal.java
│   ├── dto/SmsVerifyDTO.java, TokenResponseDTO.java
│   ├── provider/                                     # 4 个 AuthenticationProvider
│   │   ├── SmsAuthenticationProvider.java
│   │   ├── WechatAuthenticationProvider.java
│   │   ├── CasAuthenticationProvider.java
│   │   └── WeComAuthenticationProvider.java
│   ├── token/                                        # 5 个自定义 AuthenticationToken
│   │   ├── JwtAuthenticationToken.java
│   │   ├── SmsAuthenticationToken.java
│   │   ├── WechatAuthenticationToken.java
│   │   ├── CasAuthenticationToken.java
│   │   └── WeComAuthenticationToken.java
│   └── service/
│       ├── TokenProvider.java                       # JWT 签发/验证/刷新
│       └── SmsCodeService.java                      # 短信验证码管理（Redis）
├── operator/                      # 运营人员子域（含销售员 SALESMAN，表名 sys_admin）
│   ├── domain/SysAdmin.java, OperatorStatus.java, OperatorType.java
│   └── mapper/SysAdminMapper.java
├── user/                          # 小程序用户子域
│   ├── domain/SysUser.java, UserStatus.java
│   └── mapper/SysUserMapper.java
├── role/                          # 角色管理子域（从 rbac 拆分）
│   ├── api/RoleController.java
│   ├── service/SysRoleService.java, AdminRoleService.java
│   ├── domain/SysRole.java, SysOperatorRole.java, RoleMenu.java
│   └── mapper/SysRoleMapper.java, AdminRoleMapper.java, SysRoleMenuMapper.java
├── permission/                    # 权限管理子域（从 rbac 拆分）
│   ├── api/PermissionController.java, ApiPermissionController.java
│   ├── service/PermissionService.java, ApiPermissionService.java, ApiPermissionManageService.java, AdminAuthorityLoader.java
│   ├── domain/Permission.java, ApiPermission.java, RolePermission.java
│   └── mapper/PermissionMapper.java, ApiPermissionMapper.java, RolePermissionMapper.java
├── menu/                          # 菜单子域
│   ├── MenuMapping.java             # MapStruct 映射
│   ├── api/admin/MenuAdminController.java, mall/UserMenuController.java
│   ├── service/SysMenuService.java
│   ├── domain/SysMenu.java, MenuGroup.java, MenuType.java
│   ├── dto/MenuCreationDTO.java, MenuEditDTO.java, MenuSummaryDTO.java, MenuTreeNodeDTO.java, MenuViewDTO.java
│   └── mapper/SysMenuMapper.java, SysRoleMenuMapper.java
├── dict/                          # 字典子域
│   ├── DictMapping.java
│   ├── api/admin/DictController.java, DictItemController.java
│   ├── service/DictService.java, DictItemService.java
│   ├── domain/SysDict.java, SysDictItem.java
│   ├── dto/DictEditDTO.java, DictItemEditDTO.java, DictSummaryDTO.java
│   └── mapper/SysDictMapper.java, SysDictItemMapper.java
└── common/                        # 公共常量
    ├── CacheKeys.java
    ├── Permissions.java
    └── SystemConstants.java
```

#### 4.3.2 客户模块（customer）📋 待实现

```
customer/
├── CustomerModule.java            # @ApplicationModule(displayName = "Customer Module")
├── customer/                      # 客户管理子域
│   ├── api/CustomerController.java
│   ├── service/CustomerService.java
│   ├── domain/Customer.java, CustomerStatus.java
│   ├── dto/CustomerCreateDTO.java, CustomerUpdateDTO.java, CustomerSummaryDTO.java
│   └── mapper/CustomerMapper.java
├── audit/                         # 客户审核子域
│   ├── api/CustomerAuditController.java
│   ├── service/CustomerAuditService.java
│   ├── domain/CustomerAudit.java, AuditStatus.java
│   ├── dto/AuditCreateDTO.java, AuditRecordDTO.java
│   └── mapper/CustomerAuditMapper.java
└── operation/                     # 运营管理子域（仓库、运营区）
    ├── api/WarehouseController.java, OperationAreaController.java
    ├── service/WarehouseService.java, OperationAreaService.java
    ├── domain/Warehouse.java, OperationArea.java
    ├── dto/
    └── mapper/WarehouseMapper.java, OperationAreaMapper.java
```

#### 4.3.3 订单模块（order）📋 待实现

```
order/
├── OrderModule.java               # @ApplicationModule(displayName = "Order Module")
├── sales/                         # 销售订单子域
│   ├── api/SalesOrderController.java
│   ├── service/SalesOrderService.java, CouponCalculationService.java
│   ├── domain/SalesOrder.java, OrderItem.java, OrderStatus.java
│   ├── dto/
│   └── mapper/SalesOrderMapper.java, OrderItemMapper.java
├── refund/                        # 退款管理子域
│   ├── api/RefundController.java
│   ├── service/RefundService.java
│   ├── domain/RefundOrder.java, RefundStatus.java
│   ├── dto/
│   └── mapper/RefundMapper.java
├── payment/                       # 支付流水子域
│   ├── api/PaymentController.java
│   ├── service/PaymentService.java, PaymentFlowService.java
│   ├── domain/Payment.java, PaymentFlow.java
│   ├── dto/
│   └── mapper/PaymentMapper.java, PaymentFlowMapper.java
└── query/                         # 订单查询子域（只读聚合）
    ├── api/OrderQueryController.java
    └── service/OrderQueryService.java
```

#### 4.3.4 商品模块（product）📋 待实现

```
product/
├── ProductModule.java             # @ApplicationModule(displayName = "Product Module")
├── catalog/                       # 商品目录子域
│   ├── api/ProductController.java
│   ├── service/ProductService.java, SkuService.java
│   ├── domain/Product.java, Sku.java
│   ├── dto/
│   └── mapper/ProductMapper.java, SkuMapper.java
├── pricing/                       # 定价管理子域
│   ├── api/PriceController.java
│   ├── service/PriceService.java
│   ├── domain/Price.java
│   ├── dto/
│   └── mapper/PriceMapper.java
├── inventory/                     # 库存管理子域（虚拟库存，Redis + DB）
│   ├── api/InventoryController.java
│   ├── service/InventoryService.java
│   ├── domain/DailyInventoryQuota.java
│   ├── dto/
│   └── mapper/InventoryMapper.java
├── promotion/                     # 促销活动子域
│   ├── api/PromotionController.java
│   ├── service/PromotionService.java
│   ├── domain/Promotion.java
│   ├── dto/
│   └── mapper/PromotionMapper.java
└── coupon/                        # 优惠券管理子域
    ├── api/CouponController.java
    ├── service/CouponService.java
    ├── domain/Coupon.java
    ├── dto/
    └── mapper/CouponMapper.java
```

### 4.4 统一响应格式

所有 API 返回 `ResponseResult<T>`：

```java
// cn.qdm.tob.framework.model.ResponseResult
{
    "code": 0,           // 业务状态码，0 表示成功
    "data": { ... },     // 响应数据
    "msg": "success",    // 提示信息
    "traceId": "..."     // 链路追踪 ID
}
```

### 4.5 异常体系

```
TobException (RuntimeException)
├── TobServiceException       # 业务异常（可恢复）
├── TobServerException        # 服务端异常（不可恢复）
├── AuthenticationException   # 认证异常
├── InvalidSmsCodeException   # 短信验证码异常
└── TokenValidationException  # Token 验证异常
```

---

## 5. 前端架构设计

### 5.1 项目分布

| 项目 | 定位 | 技术 | 端口/平台 |
|------|------|------|---------|
| `tob-frontend-web` | PC 管理后台 | Vue 3 + Element Plus | Vite dev:8080 |
| `tob-frontend-mobile` | 微信小程序 | uni-app + uview-plus | 微信开发者工具 |
| `tob-frontend-h5` | 企业微信 H5 | Vue 3 + Vant | Vite dev:5173 |

### 5.2 前端目录规范

```
src/
├── api/                # API 接口层
│   ├── index.ts        # 统一导出
│   ├── request.ts      # HTTP 客户端（拦截器、Token 注入）
│   └── modules/        # 按业务模块分组的 API
├── components/         # 公共组件
├── config/             # 环境配置
├── layouts/            # 布局组件
├── router/             # 路由配置
├── store/              # 状态管理（Pinia）
├── styles/             # 全局样式
├── types/              # TypeScript 类型声明
├── utils/              # 工具函数
└── views/              # 页面组件
```

### 5.3 前端认证流程

| 端 | 认证方式 | 流程说明 |
|----|---------|---------|
| PC Web | CAS SSO | 开发环境支持密码登录；生产环境跳转 CAS 获取 ticket → 后端换 JWT |
| 微信小程序 | WeChat OAuth + SMS | `wx.login()` 获取 code → 后端换 openId → 签发 JWT；或手机号 + 短信验证码登录 |
| 企业微信 H5 | WeChat Work OAuth | 企业微信 JS-SDK 授权 → 获取用户身份 → 后端换 JWT |

### 5.4 Tailwind CSS 适配策略

| 端 | 配置 | 说明 |
|----|------|------|
| PC Web | `tw-` 前缀 | 避免与 Element Plus 样式冲突 |
| H5 | `postcss-pxtorem`（rootValue: 37.5） | px 自动转 rem，适配移动端 |
| 小程序 | `postcss-rem-to-responsive-pixel`（rootValue: 32, unit: rpx） | rem 转 rpx，适配小程序 |


---

## 6. 数据库设计

### 6.1 数据库连接

单实例单库 `qdm_tob`，统一通过 `application.yml` 配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST}:3306/qdm_tob?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF8
```

### 6.2 表前缀约定

同一数据库中通过表前缀区分业务域：

| 前缀 | 所属模块 | 说明 |
|------|---------|------|
| `sys_` | system | 系统服务（用户、角色、权限、菜单、字典） |
| `cst_` | customer | 客户服务（客户、审核、仓库、运营区） |
| `ord_` | order | 订单服务（销售单、退款、支付） |
| `prd_` | product | 商品服务（商品、SKU、定价、库存、优惠券、促销） |

### 6.3 核心表结构

#### 6.3.1 system 模块表（✅ 已通过 Flyway V1-V3 创建）

**认证相关（V1）：**

| 表名 | 说明             | 核心字段 |
|------|----------------|---------|
| `sys_user` | 小程序用户          | id, name, mobile, wechat_openid, wechat_unionid, status, register_time, last_login_time |
| `sys_operator` | 后台系统运营人员（含销售员） | id, mobile, real_name, email, cas_user_id, wecom_id, type(ADMIN/SALESMAN), status(ACTIVE/INACTIVE/LOCKED) |

**RBAC 权限相关（V2）：**

| 表名 | 说明 | 核心字段 |
|------|------|---------|
| `sys_permission` | 权限定义 | id, code, name, resource, action |
| `sys_api_permission` | API-权限映射 | id, http_method, url_pattern, permission_code |
| `sys_role` | 角色定义 | id, code, name, description, status |
| `sys_role_permission` | 角色-权限关联 | role_id, permission_id |
| `sys_operator_role` | 管理员-角色关联 | operator_id, role_id |

**菜单字典相关（V3）：**

| 表名 | 说明 | 核心字段 |
|------|------|---------|
| `sys_menu` | 菜单树 | id, parent_id, name, type(DIRECTORY/MENU/BUTTON), path, component, permission_code, sort, visible |
| `sys_role_menu` | 角色-菜单关联 | role_id, menu_id |
| `sys_dict` | 字典类型 | code(PK), name, description |
| `sys_dict_item` | 字典项 | id, dict_code, value, label, sort |

#### 6.3.2 customer 模块表（📋 待创建 Flyway V4）

| 表名 | 说明 | 核心字段                                                                    |
|------|------|-------------------------------------------------------------------------|
| `cst_customer` | 客户信息 | id, name, contact, phone, address, status, salesman_id（关联 sys_operator） |
| `cst_customer_audit` | 客户审核记录 | id, customer_id, auditor_id, status, remark, audit_time                 |
| `cst_warehouse` | 仓库 | id, name, address, contact, status                                      |
| `cst_operation_area` | 运营区 | id, name, warehouse_id, coverage                                        |

#### 6.3.3 order 模块表（📋 待创建 Flyway V5）

| 表名 | 说明 | 核心字段 |
|------|------|---------|
| `ord_sales_order` | 销售订单 | id, order_no, customer_id, total_amount, discount_amount, status, create_time |
| `ord_order_item` | 订单项 | id, order_id, sku_id, quantity, unit_price, subtotal |
| `ord_payment` | 支付记录 | id, order_id, payment_method, amount, status, pay_time |
| `ord_payment_flow` | 支付流水（财务对账） | id, payment_id, flow_type, amount, operator_id |
| `ord_refund` | 退款记录 | id, order_id, reason, amount, status, audit_time |

#### 6.3.4 product 模块表（📋 待创建 Flyway V6）

| 表名 | 说明 | 核心字段 |
|------|------|---------|
| `prd_product` | 商品信息 | id, name, category_id, description, status |
| `prd_sku` | SKU | id, product_id, spec, unit, price, stock |
| `prd_price` | 价格 | id, sku_id, price_type, price, effective_date |
| `prd_daily_inventory_quota` | 每日销售限额 | id, sku_id, quota_date, total_quota, sold_count |
| `prd_coupon` | 优惠券 | id, name, type, discount, stock, status |
| `prd_promotion` | 促销活动 | id, name, type, rule, start_time, end_time, status |

### 6.4 数据库规范

- **命名**：下划线命名法（snake_case），表名和字段名全部小写
- **主键**：统一使用自增 `id BIGINT`
- **时间字段**：`create_time`、`update_time`（MyBatis Plus 自动填充）
- **逻辑删除**：`is_deleted` 字段（0=未删除，1=已删除），MyBatis Plus 自动处理
- **迁移工具**：Flyway，脚本命名格式 `V{序号}__{描述}.sql`

---

## 7. 认证与授权体系

### 7.1 认证架构

安全逻辑集中在 `tob-app` 的 `SecurityConfiguration` 中处理：

```
                    请求 → SecurityConfiguration
                           ├── /api/admin/auth/**        → permitAll（登录入口）
                           ├── /api/mall/auth/**         → permitAll（登录入口）
                           ├── /actuator/** /docs/**     → permitAll
                           ├── /api/admin/**             → DynamicAuthorizationManager（动态 RBAC）
                           ├── /api/external/**          → authenticated（签名校验）
                           ├── /api/mall/**              → authenticated（JWT 认证）
                           └── anyRequest                → denyAll
                                    │
                    JwtAuthenticationFilter (UsernamePasswordAuthenticationFilter 之前)
                         ├── 提取 Authorization: Bearer {token}
                         ├── TokenProvider.validateToken() → UserPrincipal
                         └── 设置 SecurityContextHolder
                                    │
                    DynamicAuthorizationManager (针对 /api/admin/**)
                         ├── 查 api_permission 表 (method + uri → permission_code)
                         ├── 无映射 → 仅需已认证
                         ├── 有映射 → 比对 GrantedAuthority
                         └── 无权限 → 403
```

### 7.2 认证方式

| 方式 | 适用场景 | Token 类型 | 认证流程 |
|------|---------|-----------|---------|
| 短信验证码 | 小程序用户 | `SmsAuthenticationToken` | 手机号 + 验证码 → 验证 → JWT |
| 微信 OAuth | 小程序用户 | `WechatAuthenticationToken` | `wx.login()` code → `code2Session` → JWT |
| CAS | Web 管理后台 | `CasAuthenticationToken` | ticket → CAS Server 验证 → JWT |
| 企业微信 | 企业微信 H5 | `WeComAuthenticationToken` | 企业微信 code → 获取用户 → JWT |

### 7.3 JWT 设计

**Payload 结构（轻量化，~250-300 字节）：**

```json
{
    "sub": "authentication",
    "uid": "1234567890",      // 用户 ID
    "tp": "SMS",              // 认证类型（SMS/WECHAT/CAS/WECOM）
    "mbl": "138****0000",     // 手机号
    "nm": "张三",             // 姓名
    "iat": 1719600000,        // 签发时间
    "exp": 1719603600,        // 过期时间（默认 3600 秒）
    "jti": "uuid"             // Token 唯一 ID（用于黑名单）
}
```

**Token 生命周期：**
- Access Token：1 小时过期
- Refresh Token：7 天过期
- 登出：JTI 加入 Redis 黑名单（TTL = 过期时间）

### 7.4 授权机制（全动态 RBAC）

**核心流程：**
1. `JwtAuthenticationFilter` 解析 Token，设置 `SecurityContextHolder`
2. `DynamicAuthorizationManager`（实现 `AuthorizationManager<RequestAuthorizationContext>`）拦截 `/api/admin/**` 请求
3. 查 `sys_api_permission` 表（`http_method` + `url_pattern` → `permission_code`）
4. 无映射 → 仅需已认证即可访问
5. 有映射 → 比对用户 `GrantedAuthority` → 放行或 403

**关键设计决策：**
- 不使用硬编码 `@PreAuthorize`（除了系统级危险操作如删除用户）
- `sys_api_permission` 表动态维护 API 与权限的映射关系
- 权限变更实时生效（Redis 缓存 + 手动清除）
- 缓存 Key：`api_permissions:{method}:{uri}`、`operator_authorities:{operatorId}`，TTL 1 小时

### 7.5 CORS 配置

在 `SecurityConfiguration` 中直接配置：

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(List.of("*"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of(HttpHeaders.AUTHORIZATION));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

---

## 8. API 设计规范

### 8.1 URL 命名规范

```
# 基础格式（前端直连 tob-app:8081）
/{module}/{resource}[/{id}][/{sub-resource}]

# 管理后台端点（JWT + 动态 RBAC）
/api/admin/auth/**          登录/刷新/登出
/api/admin/menus/**         菜单管理
/api/admin/roles/**         角色管理
/api/admin/permissions/**   权限管理（含 API-权限映射）
/api/admin/users/**         后台用户/销售员管理
/api/admin/dicts/**         字典管理
/api/admin/customers/**     客户管理
/api/admin/orders/**        订单管理
/api/admin/products/**      商品管理

# 小程序端点（JWT 认证）
/api/mall/auth/**           认证
/api/mall/products/**       商品
/api/mall/orders/**         订单
/api/mall/coupons/**        优惠券

# 外部系统（签名认证）
/api/external/**            第三方回调/系统对接
```

### 8.2 HTTP 方法语义

| 方法 | 语义 | 幂等性 |
|------|------|--------|
| GET | 查询资源 | 是 |
| POST | 创建资源 / 触发操作 | 否 |
| PUT | 全量更新资源 | 是 |
| PATCH | 部分更新资源 | 否 |
| DELETE | 删除资源 | 是 |

### 8.3 禁止使用 @PathVariable

所有资源标识符统一通过查询参数（GET/DELETE）或请求体（POST/PUT/PATCH）传递，不使用 URL 路径参数。

```java
// ❌ 禁止
@GetMapping("/api/admin/roles/{id}")
public ResponseResult<RoleDTO> get(@PathVariable Long id)

// ✅ 正确
@GetMapping("/api/admin/roles")
public ResponseResult<RoleDTO> get(@RequestParam Long id)

// ✅ POST/PUT 通过请求体传 ID
@PutMapping("/api/admin/roles")
public ResponseResult<Void> update(@Valid @RequestBody RoleUpdateDTO dto)
// RoleUpdateDTO 中包含 id 字段
```

**理由：**
- URL 路径保持扁平，避免 REST 层级过深
- 统一参数传递方式，减少 `@PathVariable` vs `@RequestParam` 的混用
- 方便前端统一处理参数拼接

### 8.4 HTTP 状态码

| 状态码 | 说明 | 使用场景 |
|--------|------|---------|
| 200 | 成功 | GET/PUT/PATCH 成功 |
| 201 | 已创建 | POST 创建资源成功 |
| 204 | 无内容 | DELETE 成功 |
| 400 | 请求错误 | 参数校验失败 |
| 401 | 未认证 | Token 缺失/无效/过期 |
| 403 | 无权限 | 权限不足 |
| 404 | 未找到 | 资源不存在 |
| 409 | 冲突 | 资源状态冲突（如重复创建） |
| 429 | 限流 | 频率限制（如短信验证码） |
| 500 | 服务端错误 | 内部异常 |

### 8.5 分页参数

```
GET /api/admin/roles?page=1&size=20&sort=createTime,desc
```

响应格式：
```json
{
    "code": 0,
    "data": {
        "content": [...],
        "page": 1,
        "size": 20,
        "totalElements": 100,
        "totalPages": 5
    }
}
```

### 8.6 API 文档

- 使用 SpringDoc OpenAPI（`springdoc-openapi-starter-webmvc-scalar`）
- Scalar UI 访问路径：`/scalar`
- OpenAPI JSON：`/v3/api-docs`
- Swagger UI 已禁用

---

## 9. 开发规范

### 9.1 Java 编码规范

#### 命名规范

| 元素 | 规范 | 示例                                     |
|------|------|----------------------------------------|
| 类名 | PascalCase | `SysMenuService`, `TokenProvider`      |
| 方法名 | camelCase | `findByOperatorId`, `createPermission` |
| 变量名 | camelCase | `adminRoleService`, `menuList`         |
| 常量 | UPPER_SNAKE_CASE | `CACHE_KEY_PREFIX`, `MAX_ATTEMPTS`     |
| 包名 | 全小写 | `cn.qdm.tob.modules.system`            |
| 枚举 | PascalCase | `AuthType`, `MenuGroup`                |

#### 类组织规范

```
1. static 字段
2. static 方法
3. 实例字段
4. 构造器
5. 公共方法
6. 私有方法
7. 内部类/接口
```

#### 注解顺序

```
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/...")
public class XxxController { ... }
```

### 9.2 模块开发规范

#### 创建新子域的步骤

1. 在对应 `modules/{module}/` 下创建子域包
2. 创建 `api/`、`domain/`、`dto/`、`service/`、`mapper/` 子包
3. 实现 Entity（`domain/`，使用 Lombok `@Data` + MyBatis Plus 注解）
4. 实现 Mapper（`mapper/`，继承 `TobBaseMapper<T>`）
5. 实现 Service（`service/`，继承 `TobBaseService`）
6. 实现 Controller（`api/`，`@RestController` + `@RequestMapping`）
7. 编写 Flyway 迁移脚本（`db/migration/V{next}__{description}.sql`）
8. 注册 API 权限映射到 `sys_api_permission` 表

#### Controller 示例

```java
@Slf4j
@RestController
@RequestMapping("/api/admin/...")
@RequiredArgsConstructor
public class XxxController {

    private final XxxService xxxService;

    @GetMapping
    public ResponseResult<Page<XxxSummaryDTO>> list(Pageable pageable) {
        return ResponseResult.success(xxxService.list(pageable));
    }

    @PostMapping
    public ResponseResult<Long> create(@Valid @RequestBody XxxCreateDTO dto) {
        return ResponseResult.success(xxxService.create(dto));
    }

    @GetMapping("/detail")
    public ResponseResult<XxxDTO> get(@RequestParam Long id) {
        return ResponseResult.success(xxxService.get(id));
    }
}
```

### 9.3 测试规范

- 后端：JUnit 5 + Spring Boot Test
- 前端：Vitest + @vue/test-utils + jsdom
- 每个模块至少包含冒烟测试（验证模块能正确加载，ModulithVerificationTest）
- 关键业务逻辑需要有单元测试覆盖

### 9.4 Git 规范

```
分支策略：
├── main          # 生产分支
├── dev           # 开发分支（当前活跃分支）
├── feature/*     # 功能分支
├── bugfix/*      # 修复分支
└── release/*     # 发布分支
```

**Commit Message 格式：**

```
<type>(<scope>): <subject>

类型（type）：
  feat     - 新功能
  fix      - 修复 Bug
  refactor - 重构
  docs     - 文档
  test     - 测试
  chore    - 构建/工具

示例：
  feat(system): 实现动态 RBAC 授权管理器
  refactor(system): 重构角色管理，拆分 rbac 为 role 和 permission
```

### 9.5 代码审查清单

| 检查项 | 说明 |
|--------|------|
| 密码/密钥 | 不得硬编码，必须使用环境变量 |
| SQL 注入 | 使用 MyBatis Plus 参数化查询 |
| 权限控制 | 新增 API 必须在 `sys_api_permission` 表注册 |
| 异常处理 | 使用统一异常体系，避免裸 `try-catch` |
| 日志 | 关键操作记录 INFO 日志，错误记录 ERROR 日志 |
| 命名 | 遵循命名规范，避免无意义缩写 |
| 依赖方向 | 不允许反向依赖（如 framework 依赖 app） |
| @PathVariable | 禁止使用，统一使用 @RequestParam 或请求体传 ID |

---

## 10. 部署与运维

### 10.1 环境配置

| 环境 | 说明 | 配置来源 |
|------|------|---------|
| development | 本地开发 | IDE 环境变量 |
| test | QA 测试 | 流水线注入 |
| production | 生产 | K8s ConfigMap |

### 10.2 关键环境变量

| 变量 | 说明 | 当前值 |
|------|------|--------|
| `MYSQL_HOST` | MySQL 主机地址 | rm-bp1395nx3y10abehj.mysql.rds.aliyuncs.com |
| `MYSQL_USERNAME` | MySQL 用户名 | pmsadmin |
| `MYSQL_PASSWORD` | MySQL 密码 | - |
| `APP_AUTH_JWT_SECRET` | JWT 签名密钥 | 生产必须覆盖 |
| `APP_AUTH_CAS_SERVER_URL` | CAS 服务器地址 | - |
| `APP_AUTH_WECHAT_APP_ID` | 微信 AppID | - |
| `APP_AUTH_WECHAT_SECRET` | 微信 Secret | - |
| `APP_WECOM_CORP_ID` | 企业微信 CorpID | - |
| `APP_WECOM_SECRET` | 企业微信 Secret | - |

### 10.3 后端启动

```bash
# 构建
cd tob-backend
mvn clean package -DskipTests

# 启动应用
java -jar tob-app/target/tob-app-1.0.0.jar
```

### 10.4 前端启动

```bash
# PC 管理后台
cd tob-frontend/tob-frontend-web
pnpm install && pnpm dev

# 微信小程序
cd tob-frontend/tob-frontend-mobile
pnpm install && pnpm dev:mp-weixin

# 企业微信 H5
cd tob-frontend/tob-frontend-h5
pnpm install && pnpm dev
```

### 10.5 外部依赖

| 依赖 | 地址/说明 |
|------|---------|
| MySQL (RDS) | rm-bp1395nx3y10abehj.mysql.rds.aliyuncs.com:3306/qdm_tob |
| Redis | r-bp15md64bp78xg18nb.redis.rds.aliyuncs.com:6379 |
| SAP ERP | 10.111.215.25:50000 |
| WMS | wmsapi-qa.qdama.cn |
| 消息推送 | wmsapi-qa.qdama.cn |
| 企业微信 API | qyapi.weixin.qq.com |
| CAS SSO | 通过 APP_AUTH_CAS_SERVER_URL 配置 |

---
