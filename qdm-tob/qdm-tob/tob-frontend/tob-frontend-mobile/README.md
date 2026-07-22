# tob-frontend-mobile

toB 移动端微信小程序，基于 Uni-app + Vue 3 + TypeScript 构建。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | uni-app (Vue 3) | 3.0.0 |
| 语言 | TypeScript | ^5.4.0 |
| 构建 | Vite | ^5.2.0 |
| UI 组件 | uview-plus | ^3.3.27 |
| CSS 方案 | Tailwind CSS (PostCSS) | ^3.4.0 |
| 状态管理 | Pinia | ^2.1.7 |
| HTTP 请求 | luch-request | ^3.1.0 |
| 日志监控 | 阿里云 SLS (web-track-mini) | ^0.3.9 |
| UI 设计 | UI/UX Pro Max (Restaurant & Food) | - |
| 测试 | Vitest + @vue/test-utils + jsdom | - |
| 包管理 | pnpm | - |

## 项目结构

```
tob-frontend-mobile/
├── src/
│   ├── api/                  # API 接口层
│   │   ├── modules/          # 按业务模块拆分（user 等）
│   │   ├── request.ts        # HTTP 客户端（请求/响应拦截器）
│   │   └── index.ts          # 统一导出入口
│   ├── pages/                # 主包页面
│   ├── sub-pages/            # 分包页面
│   │   ├── order/            # 订单分包
│   │   ├── goods/            # 商品分包
│   │   ├── user/             # 用户分包
│   │   └── setting/          # 设置分包
│   ├── store/                # Pinia 状态管理
│   │   ├── modules/          # 按功能域拆分
│   │   └── index.ts          # 导出 createPinia
│   ├── styles/               # 全局样式（Tailwind 入口）
│   ├── utils/                # 工具函数（env 环境配置等）
│   ├── static/               # 静态资源（tabbar 图标等）
│   ├── App.vue               # 应用根组件
│   ├── main.ts               # 应用入口
│   ├── pages.json            # 页面路由 & 分包 & tabBar 配置
│   ├── manifest.json         # uni-app 应用配置
│   └── uni.scss              # uni-app 全局 SCSS 变量
├── dist/
│   ├── dev/mp-weixin/        # 开发构建产物（微信开发者工具导入）
│   └── build/mp-weixin/      # 生产构建产物（用于上传发布）
├── openspec/                 # OpenSpec 变更管理
├── package.json
├── pnpm-lock.yaml
├── vite.config.ts            # Vite 构建配置
├── tailwind.config.js        # Tailwind CSS 配置
├── postcss.config.js         # PostCSS 插件配置
└── tsconfig.json
```

## 开始开发

### 环境要求

- Node.js >= 18
- pnpm >= 8
- [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)

### 安装依赖

```bash
pnpm install
```

### 启动开发服务器

```bash
pnpm dev:mp-weixin
```

编译完成后，打开**微信开发者工具**，导入 `dist/dev/mp-weixin` 目录即可实时预览。

> **注意**：每次启动前请确保微信开发者工具已打开并导入了正确目录。如果 dev server 已启动而微信工具显示空白，尝试在微信工具中点击「编译」刷新。

### 开发规范

- **API 请求**：统一通过 `@/api/request.ts` 中的 `http` 实例发起，不要直接使用 `uni.request`
- **状态管理**：按功能域在 `store/modules/` 下创建 Pinia Store
- **样式**：优先使用 Tailwind 原子类，必要时在 `<style>` 中编写自定义样式
- **分包**：新业务模块在 `sub-pages/` 下创建，并在 `pages.json` 中注册

### 阿里云 SLS 日志监控

项目已集成阿里云 SLS 日志服务，接口请求失败时自动上报错误日志。

- **日志封装**：[src/utils/sls.ts](src/utils/sls.ts) — 单例 Tracker + STS 凭证自动刷新
- **上报时机**：HTTP 响应拦截器中，业务错误（code !== 0/200）和网络异常（4xx/5xx/timeout）均自动调用 `sendSlsLog()`
- **上报字段**：`system`（默认 `tob-小程序`）、`url`、`statusCode`、`errorResponseBody`、`requestTime`、`userId`、`version` 等
- **手动上报**：
  ```ts
  import { sendSlsLog } from '@/utils/sls'
  sendSlsLog({ url: '/api/xxx', statusCode: '500', errorResponseBody: '...' })
  ```

### UI 设计体系

采用 **UI/UX Pro Max** 设计工具，选定 **Restaurant & Food** 风格：

| 维度 | 选择 |
|------|------|
| 风格 | Restaurant & Food — 食欲红 + 暖金，温暖烟火气 |
| 主色 | `#DC2626` (Red-600) |
| 辅助色 | `#F87171` (Red-400) |
| CTA 色 | `#CA8A04` (Amber-500) 暖金 |
| 背景 | `#FEF2F2 → #FFF7ED → #FFFFFF` 暖色渐变 |
| 文字 | `#450A0A` (Red-950) 深红棕 |
| 字体 | Serif（标题）+ Sans（正文），餐厅质感 |
| 品牌 | 钱鲜达 — 新鲜食材即刻到家 |

### 测试

项目使用 **Vitest** + **@vue/test-utils** + **jsdom** 作为测试全家桶，配置文件已全局接入 Element Plus 和 Pinia 的测试环境。

#### 测试命令

```bash
pnpm test              # 交互式 watch 模式运行测试
pnpm test:run          # 单次运行全部测试
pnpm test:coverage     # 运行测试并生成覆盖率报告
pnpm test:ui           # 打开 Vitest UI 界面
```

#### 测试环境

| 组件 | 说明 |
|------|------|
| [vitest](https://vitest.dev/) | Vite 原生测试框架，共享 vite.config.ts 配置 |
| [@vue/test-utils](https://test-utils.vuejs.org/) | Vue 组件测试工具，提供 `mount()` / `shallowMount()` |
| [@testing-library/vue](https://testing-library.com/docs/vue-testing-library/intro) | 以用户视角测试组件，提供 `render()` / `screen` 等 API |
| [jsdom](https://github.com/jsdom/jsdom) | 模拟浏览器 DOM 环境 |
| [@pinia/testing](https://pinia.vuejs.org/cookbook/testing.html) | Pinia 测试工具，提供 `createTestingPinia()` |
| Element Plus | PC 端 UI 框架，已全局注册，测试中直接使用无需引入 |

#### 全局 Setup（`src/test/setup.ts`）

测试启动时自动完成以下工作：

```
┌───────────────────────────────────────────┐
│  1. Element Plus 全局注册                  │
│     → 避免 "找不到 ElButton" 报错          │
├───────────────────────────────────────────┤
│  2. Pinia 测试实例激活                     │
│     → 每个用例自动创建新实例                │
│     → 避免 "Pinia 未激活" 报错              │
├───────────────────────────────────────────┤
│  3. uni-app API Mock                      │
│     → uni.showToast / uni.request 等       │
│     → 避免 "uni is not defined" 报错       │
├───────────────────────────────────────────┤
│  4. jsdom Polyfill                        │
│     → matchMedia / IntersectionObserver    │
│     → ResizeObserver / scrollTo            │
└───────────────────────────────────────────┘
```

#### 测试文件规范

- 测试文件存放在 `src/**/__tests__/` 目录下
- 文件命名：`*.test.ts` 或 `*.spec.ts`
- Vue 组件测试用 `@vue/test-utils` 的 `mount()` 或 `@testing-library/vue` 的 `render()`
- Pinia Store 测试用 `createTestingPinia()` 或手动 `setActivePinia(createPinia())`

## 构建与发布

### 环境配置

| 环境 | 构建命令 | API 地址 | 说明 |
|------|----------|----------|------|
| 开发 | `pnpm dev:mp-weixin` | `.env.development` | 本地后端 |
| 测试 | `pnpm build:mp-weixin:test` | `.env.test` | 测试服务器 |
| 生产 | `pnpm build:mp-weixin:prod` | `.env.production` | 正式服务器 |

### 发布流程

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────┐     ┌──────────┐
│ 1. 终端构建   │ ──▶ │ 2. 微信工具预览   │ ──▶ │ 3. 点击上传   │ ──▶ │ 4. 提交审核 │
│    pnpm build │     │    导入 build 目录  │     │   版本号+备注  │     │   微信后台  │
└──────────────┘     └──────────────────┘     └──────────────┘     └──────────┘
```

**详细步骤：**

1. **终端构建**
   ```bash
   # 测试环境
   pnpm build:mp-weixin:test

   # 生产环境
   pnpm build:mp-weixin:prod
   ```

2. **微信开发者工具预览**
   - 导入 `dist/build/mp-weixin` 目录（**注意用 build 不是 dev**）
   - 在模拟器中验证功能是否正常

3. **上传**
   - 确认无误后点击微信工具右上角「上传」
   - 填写版本号（如 `1.0.0`）和变更说明
   - 上传后代码进入微信后台「版本管理」

4. **提交审核**
   - 登录 [微信公众平台](https://mp.weixin.qq.com) → 版本管理
   - 选择已上传的版本 → 提交审核
   - 审核通过后点击「发布」即可上线

### dev 与 build 产物的区别

| | `dist/dev/mp-weixin` | `dist/build/mp-weixin` |
|---|---|---|
| 用途 | 本地开发调试 | 上传发布 |
| 代码压缩 | ❌ 未压缩 | ✅ 已压缩 |
| Source Map | ✅ 有 | ❌ 无 |
| 包体积 | 较大 | 较小 |
| 能否上传 | 不推荐（体积大、暴露源码） | ✅ 推荐 |

## 常见问题

### 微信开发者工具报 WXSS 编译错误

执行 `pnpm install` 确保依赖完整，然后 `pnpm build:mp-weixin:test` 重新构建。Vite 配置中已显式声明 PostCSS 插件链，正常情况下 `@tailwind` 指令会被正确编译。

### 页面空白 / 组件不显示

1. 检查微信开发者工具是否导入了正确目录（dev 用 `dist/dev/mp-weixin`，build 用 `dist/build/mp-weixin`）
2. 检查控制台是否有报错
3. 尝试微信工具 → 清除缓存 → 重新编译

### 修改环境变量后不生效

修改 `.env.*` 文件后需要**重新执行构建命令**，环境变量在构建时注入，修改后不会自动热更新。
