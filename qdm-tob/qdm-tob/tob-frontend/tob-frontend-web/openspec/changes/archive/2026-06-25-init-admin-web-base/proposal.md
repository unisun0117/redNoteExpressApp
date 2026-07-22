## Why

当前项目目录为空，需要从零搭建一个 Web 管理端 PC 项目的基础框架。快速建立一个可扩展、可维护的技术底座，支撑后续业务模块的按需接入，是项目启动的首要任务。

## What Changes

- 使用 pnpm + Vite + Vue3 + TypeScript 初始化项目工程
- 集成 Pinia 状态管理、Element Plus 组件库、Tailwind CSS 样式框架
- 封装 Axios 请求模块，实现多环境（本地/测试/生产）域名自动切换
- 实现全局请求/响应拦截器（Token 注入、错误统一提示、401 自动清理 Token 并跳转登录页）
- 配置 Vue Router 动态路由架构，首页、登录页作为主路由，预留业务模块路由槽位，所有页面组件采用懒加载
- 搭建基础布局组件（侧边栏 + 顶栏 + 内容区）

## Capabilities

### New Capabilities

- `project-scaffold`: 基于 pnpm + Vite + Vue3 + TypeScript 的项目脚手架初始化
- `http-client`: 封装 Axios 模块，实现多环境域名切换、全局拦截器（Token 注入、错误处理、401 跳转）
- `router-system`: 基于 Vue Router 的动态路由与组件懒加载架构，含首页/登录页及业务模块路由槽位
- `ui-framework`: 集成 Element Plus + Tailwind CSS，搭建基础布局组件

### Modified Capabilities

<!-- 无现有 capabilities，此次为全新搭建 -->

## Impact

- 项目根目录：新增 package.json、vite.config.ts、tsconfig 系列、tailwind.config.js、pnpm-lock.yaml 等工程配置文件
- src/ 目录：全新创建完整的源代码结构
- 无现有代码受影响（绿色field项目）
