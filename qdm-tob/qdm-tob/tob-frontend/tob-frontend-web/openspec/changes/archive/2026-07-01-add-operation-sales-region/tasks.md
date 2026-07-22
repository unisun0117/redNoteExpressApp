## 1. 全局主色更新

- [x] 1.1 更新 `src/styles/index.css` CSS 变量：`--color-primary` 改为 `#409eff`
- [x] 1.2 同步更新 `READEME.md` UI 设计体系主色描述
- [x] 1.3 同步更新 `RULES.md` 前端设计约束主色描述

## 2. 运营管理路由与菜单

- [x] 2.1 创建 `src/router/modules/operation.ts` 路由模块（运营管理 > 销售大区）
- [x] 2.2 在 `src/router/index.ts` 中注册运营管理路由
- [x] 2.3 在 `src/layouts/DefaultLayout.vue` 菜单中新增「运营管理 > 销售大区」层级
- [x] 2.4 实现子菜单自动展开（子级路由激活时父级自动打开）

## 3. 销售大区管理页面

- [x] 3.1 创建 `src/views/operation/sales-region/index.vue` 页面组件
- [x] 3.2 实现查询区（大区筛选 + 下单服务状态筛选 + 查询/重置）
- [x] 3.3 实现数据区（19 列表格 + 新增按钮 + 分页）
- [x] 3.4 实现新增/编辑弹窗（双栏布局：基本配置 + 物流与起订规则）
- [x] 3.5 实现价格审批条件配置（阈值 + 审批人列表）
- [x] 3.6 实现 Mock 数据与完整 CRUD 交互

## 4. API 模块

- [x] 4.1 创建 `src/api/modules/operation.ts` 占位文件（含类型定义）
- [x] 4.2 在 `src/api/index.ts` 中导出运营管理 API 模块

## 5. 交互修复

- [x] 5.1 修复左侧菜单 hover 悬浮框（mouseenter 不冒泡导致 router-link 事件失效）
- [x] 5.2 修复菜单高亮逻辑：二级菜单激活时高亮子级，不高亮父级
- [x] 5.3 修复分页布局：水平居中展示，total 内嵌 pagination

## 6. 验证

- [x] 6.1 TypeScript 类型检查通过
