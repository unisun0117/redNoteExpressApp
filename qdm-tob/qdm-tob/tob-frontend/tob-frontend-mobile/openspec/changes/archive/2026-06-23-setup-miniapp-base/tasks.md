## 1. 项目初始化与配置文件

- [x] 1.1 使用 pnpm 初始化项目，创建 `package.json`，配置项目名 `tob-frontend-mobile`，设置 `type: "module"`
- [x] 1.2 安装核心依赖：`uni-app` (Vue3 版)、`pinia`、`uview-plus`、`luch-request`
- [x] 1.3 安装开发依赖：`typescript`、`vite`、`@uni-helper/vite-plugin-uni-tailwind`、`tailwindcss`、`@tailwindcss/postcss`、`sass`、`postcss`
- [x] 1.4 创建 `vite.config.ts`，配置 Uni-app Vue3 微信小程序编译插件、路径别名 `@/`、Tailwind CSS 插件
- [x] 1.5 创建 `tsconfig.json`，配置 TypeScript 编译选项、路径映射 (`@/*` → `src/*`)
- [x] 1.6 创建 `tailwind.config.js`，配置 `content` 扫描路径、禁用小程序不兼容的变体
- [x] 1.7 创建 `postcss.config.js`，引入 Tailwind CSS 插件
- [x] 1.8 创建 `index.html` 作为 Uni-app 入口 HTML

## 2. 环境变量配置

- [x] 2.1 创建 `.env.development`，定义 `VITE_API_BASE_URL`（本地域名）、`VITE_APP_ENV=development`
- [x] 2.2 创建 `.env.test`，定义 `VITE_API_BASE_URL`（测试域名）、`VITE_APP_ENV=test`
- [x] 2.3 创建 `.env.production`，定义 `VITE_API_BASE_URL`（生产域名）、`VITE_APP_ENV=production`
- [x] 2.4 创建 `src/utils/env.ts`，导出类型化的环境配置对象（`apiBaseUrl`、`mode`、`isDevelopment`、`isTest`、`isProduction`）
- [x] 2.5 在 `package.json` 中配置脚本：`dev:mp-weixin`、`build:mp-weixin:test`、`build:mp-weixin:prod`

## 3. 核心应用入口搭建

- [x] 3.1 创建 `src/manifest.json`，配置微信小程序 appid、权限声明等基础信息
- [x] 3.2 创建 `src/uni.scss`，引入 uview-plus 主题变量和全局样式
- [x] 3.3 创建 `src/styles/tailwind.css`，写入 Tailwind 基础指令（`@tailwind base/components/utilities`）
- [x] 3.4 创建 `src/App.vue`，引入 `tailwind.css`，配置全局生命周期（onLaunch 等）
- [x] 3.5 创建 `src/main.ts`，全局注册 Pinia（`createPinia`）、uview-plus，引入 `tailwind.css`

## 4. HTTP 请求封装

- [x] 4.1 创建 `src/api/request.ts`，实例化 luch-request 的 `Request` 类，配置 `baseURL`（从 `env.ts` 读取）、`timeout`、默认 `headers`
- [x] 4.2 实现请求拦截器：注入 Authorization token（从 Pinia user store 读取）、可选 loading 状态
- [x] 4.3 实现响应拦截器：统一错误处理（code ≠ 0 时 toast）、401 跳转登录、网络异常处理
- [x] 4.4 封装 GET/POST/PUT/DELETE 类型安全的请求方法，导出 `http` 实例
- [x] 4.5 创建 `src/api/modules/user.ts`，编写用户模块 API 示例（login、getUserInfo 等，仅结构不写业务逻辑）
- [x] 4.6 创建 `src/api/index.ts`，作为 API 模块的统一导出入口

## 5. 状态管理搭建

- [x] 5.1 创建 `src/store/index.ts`，导出 `createPinia()` 实例
- [x] 5.2 创建 `src/store/modules/user.ts`，定义用户 Store（token、userInfo、login action、logout action），仅骨架不含业务逻辑

## 6. 页面与分包架构

- [x] 6.1 创建 `src/pages/index/index.vue`（首页），放占位内容（标题 + "首页" 文字）
- [x] 6.2 创建 `src/pages/login/index.vue`（登录页），放占位内容（标题 + "登录页" 文字）
- [x] 6.3 创建 `src/pages.json`，配置主包页面路由（pages/index/index、pages/login/index），设置首页为 tabBar 页面
- [x] 6.4 在 `pages.json` 中配置 subPackages 分包：`sub-pages/order`、`sub-pages/user`、`sub-pages/goods`、`sub-pages/setting`，每包至少一个占位页面
- [x] 6.5 在 `pages.json` 中配置 preloadRule，首页预加载 `sub-pages/user` 分包
- [x] 6.6 创建各分包占位目录和页面文件（`sub-pages/order/pages/index/index.vue` 等）

## 7. 工具函数

- [x] 7.1 创建 `src/utils/index.ts`，导出通用工具函数（类型判断、防抖节流骨架等）
- [x] 7.2 在 `src/utils/index.ts` 中导出 `env.ts` 的环境配置对象

## 8. 验证与清理

- [x] 8.1 运行 `pnpm dev:mp-weixin` 验证项目能正常编译启动
- [x] 8.2 运行 `pnpm build:mp-weixin:test` 验证测试环境打包正常
- [x] 8.3 运行 `pnpm build:mp-weixin:prod` 验证生产环境打包正常
- [ ] 8.4 在微信开发者工具中导入 `dist/build/mp-weixin`，验证热更新和页面渲染正常（需微信开发者工具 IDE）
- [ ] 8.5 确认所有分包页面可正确跳转，无编译报错（需微信开发者工具 IDE）
