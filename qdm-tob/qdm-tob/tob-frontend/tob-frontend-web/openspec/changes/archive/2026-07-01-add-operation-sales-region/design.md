## Context

在已搭建的 Web 管理端基础框架之上，新增运营管理模块的首个子模块——销售大区管理。页面实现以产品原型 (`product/spec/07-运营管理/销售大区管理-原型.html`) 为基准，遵循项目 `RULES.md` 前端规范与设计约束。

## Goals / Non-Goals

**Goals:**
- 新增一级菜单「运营管理」及二级菜单「销售大区」
- 实现销售大区完整 CRUD 交互（列表查询、新增、编辑、删除）
- 实现新增/编辑弹窗双栏布局（基本配置 + 物流与起订规则）
- 支持价格审批条件配置
- 阶段数据使用 Mock，预留 API 接入点

**Non-Goals:**
- 不接入真实后端 API（使用 Mock 数据模拟）
- 不实现用户权限过滤（后续版本）
- 不实现仓库关联功能（依赖仓库管理模块）
- 不实现商户号实时下拉（依赖商户管理模块）

## Decisions

### 1. 路由结构
- 运营管理路由定义为带 `children` 的父级路由，`path: 'operation'`
- 销售大区作为第一个子路由 `path: 'sales-region'`
- 父路由设置 `redirect` 指向第一个子路由，确保点击「运营管理」菜单项自动打开第一个子页面

### 2. 菜单高亮策略
- 有子菜单的父级**不**因子级激活而高亮（由 `isParentActive` 控制）
- 子级菜单项通过 `router-link` 的 `active-class` 实现全色高亮（`!tw-bg-[--color-primary]`）
- 子级路由激活时，父级子菜单自动展开（`watch route.path`）

### 3. 折叠态悬浮框
- 悬浮框位置：`tw-left-full tw-top-0`（菜单项正右侧，Y 轴对齐）
- 延迟隐藏策略：`onItemLeave` 150ms 延迟清除 `hoveredItem`，`onItemEnter` 取消计时器
- 解决 Vue 3 组件事件不冒泡问题：mouseenter/mouseleave 挂载在原生 div 上而非 router-link 组件

### 4. Mock 数据策略
- 当前阶段使用组件内 `allData` ref 存储 5 条示例数据
- CRUD 操作直接在内存中完成，`loadData()` 使用 `setTimeout` 模拟异步
- 后续接入真实 API 时，仅需替换 `loadData()` / 新增 / 编辑 / 删除中的数组操作为 API 调用

### 5. 全局主色
- 主色由 `#10B981`（Emerald 绿）改为 `#409eff`（标准管理后台蓝）
- CSS 变量 `--color-primary`、`--color-primary-light`、`--color-primary-dark` 同步更新
- 文档（RULES.md、README.md）同步更新

### 6. 分页布局
- 使用 Element Plus `el-pagination` 的 `layout="total, sizes, prev, pager, next"` 内置 total 显示
- 分页容器使用 `tw-justify-center` 水平居中
- 移除工具栏的手动「共 N 条记录」文本（避免重复）
