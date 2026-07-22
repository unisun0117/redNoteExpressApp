# tob-frontend-h5

钱鲜达 toB 生鲜供应链智能管理平台移动端，基于 Vite + Vue 3 + TypeScript 构建的企业微信 H5 应用。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 (Composition API) | ^3.5.13 |
| 语言 | TypeScript | ~5.6.0 |
| 构建 | Vite | ^6.0.0 |
| 移动端 UI | Vant 4 | ^4.9.0 |
| PC 端 UI | Element Plus（测试/管理后台） | ^2.14.2 |
| CSS 方案 | Tailwind CSS + PostCSS px→rem | ^3.4.17 |
| 状态管理 | Pinia | ^2.3.0 |
| 路由 | Vue Router (History 模式) | ^4.5.0 |
| HTTP 请求 | Axios | ^1.7.9 |
| 企微 SDK | weixin-js-sdk (jweixin-1.6.0) | ^1.6.0 |
| 日志监控 | 阿里云 SLS (web-track-browser) | ^0.3.9 |
| 单元测试 | Vitest + @vue/test-utils + @testing-library/vue + jsdom | ^4.1.9 |
| UI 设计 | UI/UX Pro Max | - |
| 包管理 | pnpm | - |

## 项目结构

```
tob-frontend-h5/
├── src/
│   ├── router/
│   │   └── index.ts          # 路由实例 + 动态懒加载 + 全局导航守卫 + 业务路由插槽
│   ├── store/
│   │   ├── index.ts          # Pinia 根实例
│   │   └── user.ts           # 用户状态管理（Token + 用户信息）
│   ├── styles/
│   │   └── tailwind.css      # Tailwind 入口 + 全局样式重置 + 移动端适配
│   ├── test/
│   │   ├── setup.ts          # Vitest 全局 Setup（Element Plus + Pinia + 浏览器 API Mock）
│   │   └── smoke.spec.ts     # 冒烟测试（验证测试全家桶可用性）
│   ├── types/
│   │   ├── global.d.ts       # 全局类型扩展（vue-router meta 扩展）
│   │   └── wechat.d.ts       # 企业微信 JS-SDK 完整类型声明（wx + WWOpenData）
│   ├── utils/
│   │   ├── axios.ts          # Axios 实例（请求/响应拦截器 + Token 注入 + 401 跳转 + 多环境域名）
│   │   ├── sls.ts            # 阿里云 SLS 日志上报（单例 Tracker + STS 凭证刷新）
│   │   └── wechat.ts         # 企业微信 JS-SDK 鉴权逻辑（wx.config + agentConfig）
│   ├── views/
│   │   ├── Home.vue          # 首页（骨架占位）
│   │   └── Login.vue         # 登录页（骨架 — 预留企微 OAuth 授权）
│   ├── App.vue               # 应用根组件（初始化企微 SDK + SLS 版本号）
│   ├── main.ts               # 应用入口（注册 Pinia / Vant / Router / Tailwind）
│   ├── env.d.ts              # TypeScript 环境变量类型声明
│   └── shims-vue.d.ts        # .vue 文件类型声明
├── public/                   # 静态资源
├── openspec/                 # OpenSpec 变更管理
├── package.json
├── pnpm-lock.yaml
├── vite.config.ts            # Vite 构建配置（多环境 outDir + 代理 + Vitest）
├── tailwind.config.ts        # Tailwind CSS 配置
├── postcss.config.js         # PostCSS 插件（px→rem 自动转换, rootValue: 37.5）
├── tsconfig.json             # TypeScript 编译配置
├── tsconfig.node.json        # Node 端 TS 编译配置
├── .env                      # 通用环境变量
├── .env.development          # 本地开发环境变量
├── .env.test                 # 测试环境变量
└── .env.production           # 生产环境变量
```

## 开始开发

### 环境要求

- Node.js >= 18
- pnpm >= 8

### 安装依赖

```bash
pnpm install
```

### 启动开发服务器

```bash
pnpm dev
```

浏览器访问 `http://localhost:5173` 即可实时预览。

> **注意**：本地开发默认代理 QA 环境 API（`https://ebwmstest.qdama.cn:19011`），无需额外配置后端服务。API 请求前缀 `/api` 会被自动代理到后端。

### 开发规范

- **API 请求**：统一通过 `@/utils/axios.ts` 中的 axios 实例发起的 `get` / `post` / `put` / `del` 方法，不要直接使用 `fetch`
- **Token 管理**：通过 `sessionStorage` 存取，Key 为 `token`。使用 `@/store/user.ts` 中的 `useUserStore` 统一操作
- **状态管理**：按功能域在 `store/` 下创建 Pinia Store
- **样式**：优先使用 Tailwind 原子类，Vant 组件样式通过 CSS 变量覆盖。设计稿基准宽度 375px，PostCSS 自动将 px 转为 rem（rootValue: 37.5）
- **路由**：新业务模块路由 push 到 `src/router/index.ts` 的 `businessRoutes` 数组即可动态注册
- **企业微信**：所有企微 JS-SDK 调用必须通过 `@/utils/wechat.ts` 封装方法，不要在业务代码中直接操作 `wx.*`

## UI 设计体系

采用 **UI/UX Pro Max** 设计工具，选定 **Restaurant & Food** 风格：

| 维度 | 选择 |
|------|------|
| 风格 | Restaurant & Food — 食欲红 + 暖金，温暖烟火气 |
| 主色 | `#DC2626` (Red-600) |
| 辅助色 | `#F87171` (Red-400) |
| CTA 色 | `#CA8A04` (Yellow-600 / Amber-600) 暖金 |
| 背景 | `#FEF2F2 → #FFF7ED → #FFFFFF` 暖色渐变 |
| 文字 | `#450A0A` (Red-950) 深红棕 |
| 标题字体 | Serif — 餐厅质感，稳重优雅 |
| 正文字体 | Sans — 现代简洁，清晰易读 |
| 品牌 | **钱鲜达** — 新鲜食材即刻到家 |
| 卡片圆角 | 12px (card) / 8px (button) / 6px (input) |
| 阴影 | 柔和暖调（`0 2px 16px rgba(220,38,38,0.08)`） |
| 动画 | 200-300ms ease，自然流畅 |

### 色彩体系

```
主色 (Primary)    #DC2626  ━━━━━  品牌核心色，关键操作、导航高亮
浅主色 (Light)    #FEF2F2  ━━━━━  选中背景、标签底色
深主色 (Dark)     #991B1B  ━━━━━  hover/active 按压态
CTA 色             #CA8A04  ━━━━━  核心转化按钮、优惠标签
背景渐变起点      #FEF2F2  ━━━━━  页面顶部暖色
背景渐变终点      #FFFFFF  ━━━━━  页面底部自然白
文字主色          #450A0A  ━━━━━  标题、正文
文字辅助色        #7F1D1D  ━━━━━  次要说明文字
```

### Tailwind 主题扩展

```ts
// tailwind.config.ts
export default {
  theme: {
    extend: {
      colors: {
        brand: {
          50:  '#FEF2F2',
          100: '#FEE2E2',
          200: '#FECACA',
          300: '#FCA5A5',
          400: '#F87171',
          500: '#EF4444',
          600: '#DC2626',  // 主色
          700: '#B91C1C',
          800: '#991B1B',
          900: '#7F1D1D',
          950: '#450A0A',
        },
        cta: {
          400: '#FACC15',
          500: '#EAB308',
          600: '#CA8A04',
          700: '#A16207',
        },
      },
      fontFamily: {
        serif: ['Noto Serif SC', 'Source Han Serif CN', 'serif'],
        sans:  ['Nunito Sans', 'Inter', 'PingFang SC', 'sans-serif'],
      },
      borderRadius: {
        card: '12px',
        btn: '8px',
        input: '6px',
      },
    },
  },
}
```

## 企业微信集成

### JS-SDK 鉴权

项目在企业微信环境下自动初始化 JS-SDK 鉴权：

```
App.vue onMounted()
  → initWxConfig()              # wx.config 核心鉴权
    → fetchWxConfigSignature()  # 从后端获取签名
    → wx.config({ ... })        # 注入企业身份
    → wx.ready()                # 鉴权成功，可调用 wx.* API
    → wx.error()                # 鉴权失败，降级处理
  → initWxAgentConfig()         # 按需调用：注入应用身份（agentConfig）
```

- **环境降级**：非企业微信环境自动静默跳过，不影响普通浏览器访问
- **Debug 模式**：本地开发自动开启 `debug: true`，可在控制台查看鉴权日志

### 常用 API 封装

```ts
import { initWxConfig, initWxAgentConfig, isWxWork, hideBasicMenu, safeInvoke } from '@/utils/wechat'

// 判断是否在企业微信客户端内
if (isWxWork()) { /* ... */ }

// 初始化 JS-SDK（已在 App.vue 中全局调用，业务代码通常无需重复调用）
await initWxConfig()

// 隐藏右上角菜单
hideBasicMenu()

// 安全调用企微 API（非企微环境自动跳过）
safeInvoke(() => {
  wx.previewImage({ current: url, urls: [url] })
})
```

### 预置 JS 接口列表

| 接口 | 说明 |
|------|------|
| `hideOptionMenu` / `showOptionMenu` | 隐藏/显示右上角菜单 |
| `closeWindow` | 关闭当前网页窗口 |
| `hideMenuItems` / `showMenuItems` | 批量隐藏/显示菜单项 |
| `getNetworkType` | 获取网络状态 |
| `getLocation` | 获取地理位置 |
| `previewImage` | 图片预览 |
| `scanQRCode` | 扫码 |
| `openDefaultBrowser` | 通过企业微信打开默认浏览器 |
| `chooseImage` / `uploadImage` / `downloadImage` | 图片选择/上传/下载 |

### WWOpenData

企业微信开放数据组件（获取用户敏感信息 + 企业通讯录），类型声明已完整覆盖，调用示例：

```ts
wx.invoke('ww.getOpenData', {
  type: 'userInfo',
}, (res) => {
  console.log(res.encryptedData, res.iv)
  // 将 encryptedData + iv 发送至后端解密
})
```

## 登录

### 模式概述

项目当前登录页为**骨架占位**，预留企业微信 OAuth 2.0 授权登录流程：

```
用户访问 /login
  → 检测是否在企业微信客户端内
    ├─ 是 → 构造 OAuth 授权链接 → 跳转企微授权页
    │       → 用户授权 → 回调页面带 code
    │       → 后端接口用 code 换取 Token → 存 sessionStorage → 跳转首页
    └─ 否 → 降级为手机号验证码 / 账号密码登录
```

### 环境对照

| 环境 | API Base | 企微 CorpID | 构建命令 |
|------|----------|------------|----------|
| 本地开发 | `/api`（代理到 QA） | `.env` 配置 | `pnpm dev` |
| 测试 | `https://ebwmstest.qdama.cn:19011` | `.env.test` 配置 | `pnpm build:test` |
| 生产 | `https://ebwms.qdama.cn:19011` | `.env.production` 配置 | `pnpm build:prod` |

### Token 管理

- **存储位置**：`sessionStorage`（Key: `token`）
- **用户信息**：`sessionStorage`（Key: `userInfo`，JSON 格式）
- **401 处理**：响应拦截器检测 401/403 → 清除 Token + 用户信息 → 跳转 `/login`
- **请求注入**：请求拦截器自动为所有请求添加 `Authorization: Bearer <token>` 请求头

## 阿里云 SLS 日志监控

项目已集成阿里云 SLS 日志服务，接口请求失败时自动上报错误日志。

- **日志封装**：[src/utils/sls.ts](src/utils/sls.ts) — 单例 Tracker + STS 凭证自动刷新
- **上报时机**：HTTP 响应拦截器中，业务错误（code !== 0 且 code !== 200）、HTTP 错误（4xx/5xx）、网络异常均自动调用 `sendSlsLog()`
- **上报字段**：`system`（默认 `tob-WEB`）、`url`、`statusCode`、`errorResponseBody`、`requestTime`、`userId`、`version`、`deviceType`、`terminalType` 等
- **本地开关**：本地开发环境 `VITE_SLS_ENABLED=false`，不上报日志
- **手动上报**：
  ```ts
  import { sendSlsLog } from '@/utils/sls'
  sendSlsLog({ url: '/api/xxx', statusCode: 500, errorResponseBody: '...' })
  ```

## Axios 请求封装

### 多环境域名切换

通过 `.env.*` 文件中 `VITE_API_BASE_URL` 自动切换：

| 环境 | VITE_API_BASE_URL | 说明 |
|------|-------------------|------|
| 本地开发 | `/api` | 经过 Vite proxy 转发到 QA |
| 测试 | `https://ebwmstest.qdama.cn:19011` | 直连测试服务器 |
| 生产 | `https://ebwms.qdama.cn:19011` | 直连生产服务器 |

### 拦截器行为

```
请求拦截器：
  → 注入 Authorization: Bearer <token>
  → 注入 X-WX-Corp-ID / X-WX-Agent-ID（企微身份）

响应拦截器：
  code === 0 或 200 → 正常返回
  code === 401/403 → 清除 Token → 跳转 /login
  其他业务错误 → showToast 提示 → reject
  HTTP 4xx/5xx → showToast 提示 → reject
  网络超时/断开 → showToast 提示 → reject
```

### 使用示例

```ts
import { get, post, put, del } from '@/utils/axios'

// GET 请求
const res = await get<{ list: Item[] }>('/apply/api/xxx/list', { page: 1 })

// POST 请求
await post('/apply/api/xxx/create', { name: 'test' })

// 跳过自动错误提示
await post('/apply/api/xxx/silent', data, { skipErrorToast: true })
```

## 单元测试

### 技术栈

| 组件 | 版本 | 作用 |
|------|------|------|
| Vitest | ^4.1.9 | 测试运行器 + 断言库 |
| @vue/test-utils | ^2.4.11 | Vue 组件 mount / 交互测试 |
| @testing-library/vue | ^8.1.0 | 用户视角 DOM 查询 |
| @testing-library/jest-dom | ^6.9.1 | DOM 扩展断言（toBeVisible 等） |
| jsdom | ^29.1.1 | 模拟浏览器 DOM 环境 |

### 全局 Setup（src/test/setup.ts）

所有测试文件自动注入以下基础设施，**无需手动注册**：

- **Element Plus 全部组件** — `ElButton`、`ElInput`、`ElMessage` 等直接可用
- **Pinia 自动激活** — 每个测试 `beforeEach` 自动创建全新 Pinia 实例并 `setActivePinia()`
- **浏览器 API Mock** — `matchMedia`、`IntersectionObserver`、`ResizeObserver`、`scrollTo`、`requestAnimationFrame`
- **@testing-library/jest-dom** — `toBeVisible` / `toHaveClass` / `toBeInTheDocument` 等扩展断言

### 测试脚本

| 命令 | 说明 |
|------|------|
| `pnpm test` | 单次运行全部测试 |
| `pnpm test:watch` | Watch 模式（开发时实时重跑） |
| `pnpm test:ui` | Vitest UI 界面模式 |
| `pnpm test:coverage` | 运行测试 + 生成覆盖率报告 |
| `pnpm test:ts` | TypeScript 类型检查 (vue-tsc --noEmit) |

### 编写测试示例

#### 基础组件测试（@vue/test-utils）

```ts
// src/views/__tests__/Home.test.ts
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Home from '@/views/Home.vue'

describe('Home.vue', () => {
  it('渲染首页标题', () => {
    const wrapper = mount(Home)
    expect(wrapper.text()).toContain('企业微信H5')
  })
})
```

#### 使用 Element Plus 组件（已全局注册）

```ts
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElButton } from 'element-plus'
import { defineComponent, h } from 'vue'

describe('使用 Element Plus', () => {
  it('ElButton 直接可用', () => {
    const TestComp = defineComponent({
      components: { ElButton },
      setup() {
        return () => h('div', [h(ElButton, {}, () => '确定')])
      },
    })

    const wrapper = mount(TestComp)
    expect(wrapper.findComponent(ElButton).exists()).toBe(true)
    expect(wrapper.text()).toContain('确定')
  })
})
```

#### @testing-library/vue 组件测试

```ts
import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/vue'
import Login from '@/views/Login.vue'

describe('Login.vue', () => {
  it('渲染登录页面内容', () => {
    render(Login)
    expect(screen.getByText('企业微信授权登录')).toBeTruthy()
  })
})
```

#### Pinia Store 测试

```ts
import { describe, it, expect } from 'vitest'
import { useUserStore } from '@/store/user'

describe('useUserStore', () => {
  it('初始状态 token 为空', () => {
    const store = useUserStore()
    expect(store.token).toBe('')
  })

  it('setToken 后 token 正确赋值', () => {
    const store = useUserStore()
    store.setToken('mock-token-xyz')
    expect(store.token).toBe('mock-token-xyz')
  })

  it('logout 后 token 清空', () => {
    const store = useUserStore()
    store.setToken('mock-token')
    store.logout()
    expect(store.token).toBe('')
  })
})
```

## 移动端适配

### Viewport

```html
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0,
  minimum-scale=1.0, user-scalable=no, viewport-fit=cover" />
```

### px → rem 自动转换

PostCSS 插件 `postcss-pxtorem` 自动将 CSS 中的 px 转为 rem：

- **rootValue**: 37.5（适配 375px 设计稿，1rem = 37.5px）
- **propList**: `['*']` — 所有 CSS 属性均转换
- **selectorBlackList**: `['.norem']` — 带 `.norem` 类名的不转换

> 设计稿基准 375px，书写 CSS 时直接按设计稿标注写 px，构建时自动转为 rem。

## 构建与发布

### 环境配置

| 环境 | 构建命令 | API 地址 | SLS 日志 | 输出目录 |
|------|----------|----------|----------|----------|
| 开发 | `pnpm dev` | `/api`（代理 QA） | 关闭 | - |
| 测试 | `pnpm build:test` | `ebwmstest.qdama.cn:19011` | 开启 | `dist-qa/` |
| 生产 | `pnpm build:prod` | `ebwms.qdama.cn:19011` | 开启 | `dist-pro/` |

### 可用脚本

| 命令 | 说明 |
|------|------|
| `pnpm dev` | 启动开发服务器（端口 5173） |
| `pnpm build:test` | 构建测试环境（输出 `dist-qa/`） |
| `pnpm build:prod` | 构建生产环境（输出 `dist-pro/`） |
| `pnpm preview` | 本地预览构建产物 |
| `pnpm test` | 单次运行全部测试 |
| `pnpm test:watch` | 启动 Vitest Watch 模式 |
| `pnpm test:ui` | 启动 Vitest UI 界面 |
| `pnpm test:coverage` | 运行测试 + 生成覆盖率报告 |
| `pnpm test:ts` | TypeScript 类型检查 |

### 发布流程

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────┐
│ 1. 终端构建   │ ──▶ │ 2. 本地预览验证   │ ──▶ │ 3. 部署服务器  │
│    pnpm       │     │    pnpm preview   │     │    dist-qa/   │
│    build:test │     │                   │     │    dist-pro/  │
│    build:prod │     │                   │     │               │
└──────────────┘     └──────────────────┘     └──────────────┘
```

**详细步骤：**

1. **终端构建**
   ```bash
   # 测试环境
   pnpm build:test

   # 生产环境
   pnpm build:prod
   ```

2. **本地预览验证**
   ```bash
   pnpm preview
   ```

3. **部署**
   - 将 `dist-pro/` 或 `dist-qa/` 目录部署到对应服务器
   - 确保 Web 服务器（Nginx）配置了 SPA 路由回退

### Nginx 配置参考

```nginx
server {
    listen 80;
    server_name h5.example.com;

    root /var/www/tob-h5;
    index index.html;

    # SPA 路由回退
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 静态资源强缓存
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

## 常见问题

### 测试报 "找不到 ElButton" 或 "Pinia 未激活"

确认 `vite.config.ts` 中 `test.setupFiles` 配置正确指向 `./src/test/setup.ts`。setup 文件已自动注册 Element Plus 全部组件并在每个 `beforeEach` 中激活全新 Pinia 实例。

### 企业微信 SDK 调用没反应

1. 检查是否在企业微信客户端内打开（`isWxWork()` 为 false 时所有企微 API 静默跳过）
2. 检查 `wx.config` 鉴权是否成功（本地开发自动开启 debug 模式，控制台可见日志）
3. 检查后端签名接口 `/apply/api/common/wx/jsapi-signature` 是否正常返回

### PostCSS px→rem 转换导致部分元素尺寸异常

给不需要转换的元素添加 `.norem` 类名即可跳过转换：

```css
.fixed-px.norem {
  width: 1px;  /* 不会被转为 rem */
}
```

### 修改环境变量后不生效

修改 `.env.*` 文件后需要**重启 dev server** 或**重新构建**，环境变量在启动时注入。

### Tailwind 样式不生效

检查 `tailwind.config.ts` 中 `content` 路径是否包含目标文件：

```ts
content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}']
```
