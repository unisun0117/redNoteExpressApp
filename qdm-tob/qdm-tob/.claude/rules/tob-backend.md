---
description: Java 后端开发规范
paths:
  - "tob-backend/**/*"
---

## 模块分层

子域按五层组织，**禁止越职**：

| 层 | 目录 | 职责 | 禁止 |
|---|---|---|---|
| **Controller** | `api/admin/` `api/mall/` `api/external/` | 接收请求、调 Service、返回 `ResponseResult` | 业务逻辑、直接 Mapper、返回 Domain |
| **Service** | `service/` | 业务逻辑、事务、调 Mapper 或 Internal API | 实现 API 接口、处理 HTTP、跨模块直接访问 Mapper |
| **Mapper** | `mapper/` | 数据库 CRUD | 业务逻辑、调其他 Mapper |
| **VO** | `vo/` | 入参/出参字段定义 + 校验注解 | 业务逻辑 |
| **Domain** | `domain/` | 实体字段映射（`@TableName`） | 业务逻辑、作为 Controller 出参 |
| **Enum** | `enums/` | 领域枚举，实现 `Describable`，允许跨模块引用 | — |

```
Controller → Service → Mapper / Internal API
                ↑          ↑
              VO/DTO    Domain/Entity
```

## Internal API
- 跨模块调用定义在 `api/internal/`，**直接定义为 Class**（非 Interface + Impl），委托 Service
- 只做参数转换，禁止业务逻辑
- 出入参用 `*DTO`，定义在 `api/internal/dto/`

## 模块边界
- 依赖方向：`modules` → `infrastructure`（单向），禁止反向
- **infrastructure 禁止引用 modules 下的任何类**
- modules 间不允许跨模块直接访问 Mapper/Domain/Service，只能通过 Internal API 或 Spring Event
- 同模块内子域间可直接调用 Service

## 统一响应
- Controller 必须返回 `ResponseResult<T>`，禁止直接返回实体

## 获取当前用户
- Controller：`@CurrentUser UserPrincipal user`
- Service：`SecurityUtil.getCurrentUserId()`

## 获取当前客户（小程序）
- Controller：`@CurrentCustomer CustomerContext customer`
- 解析器自动校验地址绑定 + 审核状态，注入 `salesRegionCode` / `priceGroup` 等
- 前端传 `addressId` 必须为 query 参数
- 禁止手动查询客户档案做越权校验

## 表前缀隔离
- `sys_`/`cst_`/`ord_`/`prd_` 不同前缀**禁止 JOIN**
- 跨模块数据通过应用层组合或 Spring Event

## API 规范
- 资源 ID 用 `@RequestParam` 或请求体，禁止 `@PathVariable`
- `api/admin/` → `/api/admin/`、`api/mall/` → `/api/mall/`、`api/external/` → `/api/external/`
- 入参 VO：`*CreationVO` / `*EditVO` / `*QueryVO`，出参 VO：`*ViewVO` / `*SummaryVO`
- Entity ↔ VO 用 MapStruct，禁止手写转换
- 权限用 `@RequirePermission` 注解

## 异常体系
- 业务异常 → `TobServiceException`，服务端异常 → `TobServerException`

## Excel
- 导出用 `@Exportable`，导入用 `@Importable` + `@ExcelColumn` 或 `ExcelImporter`
- 禁止自行封装 Excel 读写

## OSS 文件上传

- Controller **禁止** `MultipartFile` 参数，全部走 STS Token + 前端直传
- 上传场景必须在 `StsPolicy` 枚举注册（目录/大小/类型/有效期），前端 `?policy=XXX` 请求，禁止硬编码
- STS Token API：admin `/api/admin/oss/sts-token`，mall `/api/mall/oss/sts-token`
- Domain/DTO 只存 **OSS key**，出参 VO 字段加 `@OssResource` 自动序列化为 URL
