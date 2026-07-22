## Context

当前项目仓库为空，需从零搭建 Web 管理端前端项目。项目目标是为后续业务模块提供统一的技术底座，包括项目工程化、网络请求层、路由系统、UI 框架。技术栈已确定：pnpm + Vite + Vue3 + TypeScript + Pinia + Element Plus + Tailwind CSS。

## Goals / Non-Goals

**Goals:**
- 建立标准的 Vite + Vue3 + TypeScript 工程脚手架，支持 pnpm 包管理
- 封装 Axios 模块，实现多环境域名自动切换、全局拦截器、401 自动跳转登录
- 搭建 Vue Router 动态路由 + 组件懒加载架构，首页/登录页为主路由，预留业务模块槽位
- 集成 Element Plus + Tailwind CSS + Pinia，搭建基础布局组件

**Non-Goals:**
- 不编写具体业务逻辑（登录鉴权仅保留框架层 Token 校验与跳转，不实现登录表单逻辑）
- 不实现权限管理（RBAC 等后续按需接入）
- 不涉及后端接口开发
- 不做 SSR/SSG

## Decisions

### 1. 包管理工具: pnpm
- **选择**: pnpm
- **替代方案**: npm、yarn
- **理由**: 磁盘空间占用小（hard link 机制），安装速度快，monorepo 友好（为后续多包扩展预留可能）

### 2. 构建工具: Vite
- **选择**: Vite 5.x
- **替代方案**: Webpack 5、Turbopack
- **理由**: 开发服务器 HMR 速度快（ESM native），Vue3 官方推荐，生态成熟

### 3. UI 组件库: Element Plus
- **选择**: Element Plus
- **替代方案**: Ant Design Vue、Naive UI
- **理由**: Vue3 生态中社区最活跃的企业级组件库，中文文档完善，与后台管理场景高度匹配

### 4. CSS 方案: Tailwind CSS + Element Plus 共存
- **选择**: Tailwind CSS 用于布局/间距/工具类样式，Element Plus 用于标准组件
- **替代方案**: 纯 Element Plus 内置样式、UnoCSS
- **理由**: Tailwind 提供原子化 CSS 灵活性，Element Plus 提供标准业务组件，两者职责分离互补。通过 Tailwind `prefix` 配置（如 `tw-`）避免与 Element Plus CSS 类名冲突

### 5. 目录结构
```
src/
├── api/              # API 接口定义
│   ├── index.ts      # Axios 实例 + 拦截器
│   └── modules/      # 按业务模块拆分 API
├── assets/           # 静态资源
├── components/       # 公共组件
├── composables/      # 组合式函数
├── layouts/          # 布局组件
│   └── DefaultLayout.vue
├── router/           # 路由配置
│   └── index.ts
├── stores/           # Pinia 状态管理
├── styles/           # 全局样式
├── types/            # TypeScript 类型定义
├── utils/            # 工具函数
│   ├── request.ts    # Axios 封装
│   └── auth.ts       # Token 管理工具
├── views/            # 页面组件
│   ├── home/
│   └── login/
├── App.vue
├── main.ts
└── env.d.ts
```

### 6. 多环境管理
使用 Vite 环境变量 + `.env.*` 文件实现：
- `.env.development` — 本地开发
- `.env.test` — 测试环境
- `.env.production` — 生产环境

通过 `import.meta.env.VITE_API_BASE_URL` 在 Axios 实例中自动切换 baseURL。

### 7. Axios 拦截器设计
- **请求拦截**: 从 localStorage 读取 Token 注入 `Authorization: Bearer <token>` 头
- **响应拦截**: 统一处理错误码 → 非 200 自动 `ElMessage.error` 提示；401 时清除 Token → `router.push('/login')` 跳转登录页
- **Token 存储**: localStorage，key 为 `TOKEN_KEY`

### 8. 路由设计
- 首页 `/` — 默认主页面，作为登录后的 Landing Page
- 登录页 `/login` — 独立路由，不使用主布局
- 404 页面 `/:pathMatch(.*)*` — 兜底路由
- 业务模块槽位: 使用 `router.addRoute()` 动态注入模式，`src/router/modules/` 目录下各模块独立文件，按需在 `main.ts` 中注册

## Risks / Trade-offs

- **Tailwind CSS 与 Element Plus 样式冲突**: 通过 Tailwind `prefix` 配置规避 → 设置 `prefix: 'tw-'`
- **动态路由安全性**: 前端路由守卫仅做 Token 存在性检查，不做权限校验 → 后续接入后端权限时增强路由守卫逻辑
- **Token 存储在 localStorage**: 存在 XSS 风险 → 当前阶段接受此方案，后续可升级为 httpOnly Cookie + refresh token 方案
