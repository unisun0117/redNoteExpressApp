## Why

运营管理人员需要一个统一的入口来管理销售大区的全生命周期配置，包括大区的新增、编辑、删除、查询，以及营业规则、物流费用、起订规则、价格审批等配置的集中维护。

## What Changes

- 新增一级菜单「运营管理」，其下新增二级菜单「销售大区」
- 新增运营管理路由模块 `src/router/modules/operation.ts`，支持子路由动态扩展
- 新增销售大区管理页面 `src/views/operation/sales-region/index.vue`
  - 查询区：按大区名称/编号筛选 + 下单服务启用状态筛选
  - 数据区：19 列表格（编号可点击查看详情）+ 新增大区按钮 + 分页
  - 新增/编辑弹窗：双栏布局（左栏基本配置 + 右栏物流与起订规则）
  - 价格审批配置：条件展示审批阈值 + 审批人列表
- 新增运营管理 API 模块占位 `src/api/modules/operation.ts`
- 优化全局主色为 `#409eff`
- 优化左侧菜单交互：二级菜单激活时高亮子级（非父级），收起态悬浮框修复
- 优化分页布局：水平居中展示

## Capabilities

### New Capabilities

- `sales-region-management`: 销售大区全生命周期管理（列表查询、新增、编辑、删除、物流费用配置、价格审批配置）

### Modified Capabilities

- `ui-framework`: 更新全局主色至 `#409eff`，更新菜单高亮逻辑为子级高亮
- `router-system`: 新增运营管理路由模块，支持父子路由嵌套

## Impact

- `src/views/operation/` — 新增运营管理页面目录
- `src/router/modules/operation.ts` — 新增运营管理路由模块
- `src/api/modules/operation.ts` — 新增运营管理 API 模块
- `src/styles/index.css` — 更新主色 CSS 变量
- `src/layouts/DefaultLayout.vue` — 新增菜单项、修复 hover 事件、子级高亮逻辑
- `src/api/index.ts` — 导出运营管理 API 模块
- `src/router/index.ts` — 注册运营管理路由
