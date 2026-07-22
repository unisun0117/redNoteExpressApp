## Why

项目处于启动阶段，需要搭建一套标准化的微信小程序开发底座。当前缺乏统一的项目脚手架、HTTP 请求封装、多环境配置和分包架构，导致后续业务开发无法高效展开。本次变更旨在一次性建立所有基础设施，为团队提供开箱即用的开发框架。

## What Changes

- 使用 pnpm 初始化 Uni-app (Vue3 + TypeScript) 微信小程序项目
- 集成 Pinia 状态管理、uview-plus UI 组件库、Tailwind CSS 样式框架
- 基于 luch-request 封装统一 HTTP 请求模块，支持模块化导入
- 实现多环境（本地/测试/生产）域名自动切换与全局请求/响应拦截
- 配置微信开发者工具热更新支持
- 搭建 pages.json 主包 + subPackages 分包架构，预留业务子包目录
- **不编写具体业务逻辑**，仅提供框架底座

## Capabilities

### New Capabilities

- `project-scaffold`: 使用 pnpm + Uni-app (Vue3 + TypeScript) 初始化微信小程序项目脚手架，集成 Pinia、uview-plus、Tailwind CSS
- `http-client`: 基于 luch-request 封装统一 HTTP 请求模块，支持多环境域名自动切换、全局请求/响应拦截器、模块化 API 导出
- `env-config`: 配置本地（development）、测试（test）、生产（production）三套环境变量，打包时自动区分环境并注入对应域名
- `subpackage-arch`: 在 pages.json 中搭建主包（首页、登录页）与 subPackages 分包架构，为后续业务模块预留独立子包目录

### Modified Capabilities

<!-- 新项目，无已有规范需要修改 -->

## Impact

- **项目根目录**: 新增 `package.json`、`vite.config.ts`、`tsconfig.json`、`tailwind.config.js` 等配置文件
- **源码目录**: 创建 `src/` 完整目录结构（api、pages、store、utils、sub-pages 等）
- **环境配置**: 新增 `.env.development`、`.env.test`、`.env.production` 及 `src/utils/env.ts`
- **HTTP 模块**: 新增 `src/utils/request.ts`（luch-request 封装）及 `src/api/` 模块示例
- **分包目录**: 新增 `src/sub-pages/` 下各业务分包占位目录
- **页面配置**: 修改 `src/pages.json` 为主包 + subPackages 结构
- **App 入口**: 修改 `App.vue` 和 `main.ts` 完成 Pinia、uview-plus 全局注册
- **依赖**: 新增 uview-plus、luch-request、pinia、tailwindcss、@tailwindcss/postcss 等依赖
