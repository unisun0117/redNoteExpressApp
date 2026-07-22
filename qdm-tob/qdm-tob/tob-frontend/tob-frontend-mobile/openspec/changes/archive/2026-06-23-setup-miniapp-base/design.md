## Context

项目从零启动，需要搭建一套标准化的 Uni-app 微信小程序开发底座。当前无任何基础设施，技术选型已确定：Uni-app (Vue3 + TypeScript) + Pinia + uview-plus + luch-request + Tailwind CSS + pnpm。本次设计聚焦于项目骨架搭建，不涉及具体业务逻辑。

约束条件：
- 微信小程序包体积限制（主包 ≤ 2MB，总包 ≤ 20MB）
- 微信开发者工具需要热更新支持
- 需要本地/测试/生产三套环境独立配置
- 后续业务模块需独立分包以优化加载性能

## Goals / Non-Goals

**Goals:**
- 搭建可运行的 Uni-app (Vue3 + TypeScript) 微信小程序项目骨架
- 封装 luch-request 为统一 HTTP 客户端，支持环境域名切换和拦截器
- 实现三套环境变量配置，打包时自动注入
- 支持微信开发者工具热更新
- 建立主包 + 分包页面架构，预留业务子包目录
- 全局注册 Pinia、uview-plus，配置 Tailwind CSS

**Non-Goals:**
- 不编写任何具体业务逻辑代码
- 不设计完整的权限/认证体系（仅预留 token 注入点）
- 不配置 CI/CD 流水线
- 不做 UI 设计，页面仅放占位内容

## Decisions

### 1. 包管理器：pnpm

**选择**: pnpm  
**替代方案**: npm, yarn  
**理由**: 磁盘空间节省 50%+，安装速度快，原生支持 workspace（未来 monorepo 扩展方便），严格依赖管理避免幽灵依赖

### 2. 构建工具：Vite

**选择**: Vite（Uni-app Vue3 版默认）  
**替代方案**: Webpack（Vue CLI）  
**理由**: Uni-app 官方已将 Vue3 版默认构建工具从 webpack 迁移至 Vite，HMR 速度更快，配置更简洁。热更新在微信开发者工具中通过 Uni-app 的 `dev:mp-weixin` 命令原生支持。

### 3. HTTP 请求库：luch-request

**选择**: luch-request  
**替代方案**: uni.request 原生封装、axios（小程序适配版）  
**理由**: luch-request 专为 Uni-app 设计，API 风格接近 axios，天然支持请求/响应拦截器、Promise、全局配置。axios 在小程序环境需要额外适配层且体积较大。

**封装架构**:
```
src/api/
├── request.ts          ← 核心：luch-request 实例创建 + 拦截器
├── index.ts            ← API 模块聚合导出
└── modules/
    └── user.ts         ← 业务模块 API 示例
```

拦截器设计：
- **请求拦截器**: 注入 token（从 Pinia store 读取）、添加 loading 状态、注入环境对应的 baseURL
- **响应拦截器**: 统一错误处理（code ≠ 0 时 toast 提示）、401 时跳转登录、token 过期刷新

### 4. 环境配置策略

**选择**: `.env.*` 文件 + Vite 环境变量 + Uni-app `import.meta.env`  
**替代方案**: 条件编译 `#ifdef` 硬编码域名  
**理由**: `.env.*` 文件方式清晰直观，Vite 原生支持。结合 `src/utils/env.ts` 导出统一的环境配置对象，打包时 Vite 自动替换。各环境文件：

| 文件 | 用途 | 域名示例 |
|------|------|----------|
| `.env.development` | 本地开发 | `http://localhost:8080` |
| `.env.test` | 测试环境 | `https://test-api.example.com` |
| `.env.production` | 生产环境 | `https://api.example.com` |

### 5. UI 框架组合：uview-plus + Tailwind CSS

**选择**: uview-plus + Tailwind CSS 组合  
**理由**:
- uview-plus 提供丰富的开箱即用组件（表单、列表、弹窗等），减少重复造轮子
- Tailwind CSS 提供原子化样式，快速定制 UI，与 uview-plus 互补
- Tailwind 在小程序端通过 PostCSS 插件 + rem → rpx 适配

**注意**: 小程序不支持 Tailwind 的某些 CSS 特性（如 `@apply` 的部分用法），需在 tailwind.config.js 中配置 `separator` 和禁用不支持的变体。

### 6. 分包架构

**选择**: 遵循微信小程序分包规范，主包仅保留首页、登录页  
**理由**: 主包体积是性能关键瓶颈，将非核心页面移入分包可加快首次加载速度

**分包划分**（为后续业务预留）:
```
主包 (pages/)
├── pages/index/index        ← 首页
└── pages/login/index        ← 登录页

分包 (sub-pages/)
├── sub-pages/order/         ← 订单模块
├── sub-pages/user/          ← 用户模块
├── sub-pages/goods/         ← 商品模块
└── sub-pages/setting/       ← 设置模块
```

### 7. 目录结构总览

```
tob-frontend-mobile/
├── .env.development
├── .env.test
├── .env.production
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.js
├── postcss.config.js
├── index.html
├── src/
│   ├── App.vue
│   ├── main.ts
│   ├── manifest.json
│   ├── pages.json
│   ├── uni.scss
│   ├── api/
│   │   ├── request.ts
│   │   ├── index.ts
│   │   └── modules/
│   │       └── user.ts
│   ├── pages/
│   │   ├── index/index.vue
│   │   └── login/index.vue
│   ├── store/
│   │   ├── index.ts
│   │   └── modules/
│   │       └── user.ts
│   ├── sub-pages/
│   │   ├── order/pages/     ← 占位 .gitkeep
│   │   ├── user/pages/
│   │   ├── goods/pages/
│   │   └── setting/pages/
│   ├── utils/
│   │   ├── env.ts
│   │   └── index.ts
│   └── styles/
│       └── tailwind.css
```

## Risks / Trade-offs

- **[R] Tailwind CSS 在小程序环境的兼容性** → 限制使用不支持的 CSS 特性，配置 `tailwind.config.js` 仅保留兼容的 utility class，统一使用 rpx 单位
- **[R] uview-plus 组件库体积较大** → 按需引入（通过 `easycom` 自动按需加载），不全局全量注册
- **[R] 分包间代码共享** → 公共组件/工具函数放在主包 `src/utils/` 和 `src/components/`，分包子包通过相对路径引用主包资源
- **[R] 环境变量泄露到前端代码** → 仅 `VITE_` 前缀的变量会被注入客户端，敏感 key 不走环境变量，由后端下发
