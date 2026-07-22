## 1. 项目脚手架初始化

- [x] 1.1 初始化 pnpm 项目，创建 `package.json`，配置项目名称与基础字段
- [x] 1.2 安装 Vite + Vue3 + TypeScript 依赖，创建 `vite.config.ts`
- [x] 1.3 创建 `tsconfig.json`、`tsconfig.app.json`、`tsconfig.node.json`，配置 TypeScript 编译选项
- [x] 1.4 创建 `.env.development`、`.env.test`、`.env.production` 环境变量文件，定义 `VITE_API_BASE_URL`
- [x] 1.5 创建 `index.html` 入口文件与 `src/main.ts` 应用入口
- [x] 1.6 创建 `src/env.d.ts` 声明 `import.meta.env` 类型
- [x] 1.7 配置 ESLint + Prettier，创建 `.eslintrc.cjs` 与 `.prettierrc.json`

## 2. UI 框架集成

- [x] 2.1 安装 Element Plus + @element-plus/icons-vue，在 `main.ts` 中全局注册
- [x] 2.2 安装 Tailwind CSS + postcss + autoprefixer，创建 `tailwind.config.js`（配置 `prefix: 'tw-'`）与 `postcss.config.js`
- [x] 2.3 安装 Pinia，在 `main.ts` 中注册 `createPinia()`
- [x] 2.4 创建 `src/styles/` 全局样式文件（含 CSS 变量、Tailwind 指令、Element Plus 主题覆盖）
- [x] 2.5 创建 `src/layouts/DefaultLayout.vue` 布局组件（侧边栏 + 顶栏 + `<router-view>` 内容区）
- [x] 2.6 创建 `src/App.vue` 根组件，挂载 `<router-view>`

## 3. HTTP 客户端封装

- [x] 3.1 安装 Axios，创建 `src/utils/auth.ts` Token 管理工具（`getToken`、`setToken`、`removeToken`，key 为 `TOKEN_KEY`）
- [x] 3.2 创建 `src/utils/request.ts` Axios 实例封装（baseURL 从 `import.meta.env.VITE_API_BASE_URL` 读取，超时 30s）
- [x] 3.3 实现请求拦截器：注入 `Authorization: Bearer <token>` 头
- [x] 3.4 实现响应拦截器：统一错误 `ElMessage.error` 提示
- [x] 3.5 实现响应拦截器：401 时调用 `removeToken()` 并跳转 `/login`（排除当前已在 `/login` 的情况避免死循环）

## 4. 路由系统搭建

- [x] 4.1 安装 Vue Router，创建 `src/router/index.ts` 路由实例（`createRouter` + `createWebHistory`）
- [x] 4.2 定义首页路由 `/`：懒加载 `src/views/home/index.vue`，使用 DefaultLayout 布局
- [x] 4.3 定义登录页路由 `/login`：懒加载 `src/views/login/index.vue`，不使用布局（全屏独立页面）
- [x] 4.4 定义 404 路由 `/:pathMatch(.*)*`：懒加载 `src/views/error/404.vue`
- [x] 4.5 创建业务模块路由目录 `src/router/modules/`，编写示例模块路由文件与动态注册注释说明
- [x] 4.6 实现全局导航守卫 `beforeEach`：无 Token 时重定向到 `/login`，已登录访问 `/login` 时重定向到 `/`

## 5. 页面与验证

- [x] 5.1 创建 `src/views/home/index.vue` 首页占位组件（含欢迎文案）
- [x] 5.2 创建 `src/views/login/index.vue` 登录页占位组件（含登录表单区域占位，不实现具体逻辑）
- [x] 5.3 创建 `src/views/error/404.vue` 404 页面组件
- [x] 5.4 运行 `pnpm dev` 验证开发服务器启动、路由跳转正常、布局渲染正确
- [x] 5.5 运行 `pnpm build` 验证生产构建通过、TypeScript 无报错
