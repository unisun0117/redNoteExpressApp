# tob-frontend-web

钱鲜达 toB 生鲜供应链智能管理平台 PC 端，基于 Vite + Vue 3 + TypeScript 构建。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 (Composition API) | ^3.5.13 |
| 语言 | TypeScript | ~5.6.3 |
| 构建 | Vite | ^6.0.5 |
| UI 组件 | Element Plus | ^2.9.1 |
| CSS 方案 | Tailwind CSS (PostCSS) | ^3.4.17 |
| 状态管理 | Pinia | ^2.3.0 |
| 路由 | Vue Router (History 模式) | ^4.5.0 |
| HTTP 请求 | Axios | ^1.7.9 |
| 日志监控 | 阿里云 SLS (web-track-browser) | ^0.3.9 |
| 统一登录 | CAS (Central Authentication Service) | - |
| 单元测试 | Vitest + @vue/test-utils + @testing-library/vue + jsdom | ^4.1.9 |
| UI 设计 | UI/UX Pro Max (Soft UI Evolution + AI-Native) | - |
| 包管理 | pnpm | - |

## 项目结构

```
tob-frontend-web/
├── src/
│   ├── api/                  # API 接口层
│   │   ├── auth.ts           # 登录 API（CAS Ticket / 密码登录）
│   │   └── index.ts          # 统一导出入口
│   ├── config/
│   │   └── env.ts            # 多环境配置（API/CAS/Web URL + 凭证）
│   ├── layouts/
│   │   └── DefaultLayout.vue # 默认布局（侧边栏 + 顶栏 + 内容区）
│   ├── router/
│   │   ├── index.ts          # 路由实例 + 全局导航守卫 + CAS Ticket 处理 + 防死循环
│   │   └── modules/          # 业务模块路由（按需动态注册）
│   │       └── example.ts    # 示例模块路由文件
│   ├── stores/               # Pinia 状态管理
│   ├── styles/
│   │   └── index.css         # 全局样式（CSS 变量 + Tailwind 入口 + Element Plus 主题覆盖）
│   ├── test/
│   │   ├── setup.ts          # Vitest 全局 Setup（Element Plus + Pinia + 图标 + ElMessage Mock）
│   │   ├── vitest.d.ts       # Vitest globals 类型声明
│   │   └── __tests__/        # 测试用例目录
│   │       └── smoke.test.ts # 冒烟测试（验证全家桶可用性）
│   ├── utils/
│   │   ├── auth.ts           # Token 管理（localStorage 存取，Key: Token）
│   │   ├── request.ts        # Axios 实例（请求/响应拦截器 + SLS 上报 + CAS 401 跳转 + code 兼容）
│   │   └── sls.ts            # 阿里云 SLS 日志上报（单例 Tracker + STS 凭证刷新）
│   ├── views/
│   │   ├── home/             # 首页（Dashboard 占位）
│   │   ├── login/            # 登录页（本地=手动表单 / 测试生产=CAS 跳转）
│   │   └── error/            # 404 等错误页
│   ├── App.vue               # 应用根组件
│   ├── main.ts               # 应用入口（注册 Pinia / Element Plus / Router）
│   └── env.d.ts              # TypeScript 环境变量类型声明
├── public/                   # 静态资源
├── openspec/                 # OpenSpec 变更管理
│   └── changes/
│       └── archive/          # 已归档变更
├── package.json
├── pnpm-lock.yaml
├── vite.config.ts            # Vite 构建配置（多环境 outDir + Vitest）
├── tailwind.config.js        # Tailwind CSS 配置（tw- 前缀）
├── postcss.config.js         # PostCSS 插件配置
├── tsconfig.json             # TypeScript 项目引用
├── tsconfig.app.json         # 应用 TS 编译配置
├── tsconfig.node.json        # Node 端 TS 编译配置
├── .eslintrc.cjs             # ESLint 规则
├── .prettierrc.json          # Prettier 格式化规则
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

浏览器访问 `http://localhost:8080` 即可实时预览。

> **注意**：本地开发默认连接 QA 环境 API（`https://b2b-buy-qa.qdama.cn/qdm-b2b/api`），无需额外配置后端服务。

### 开发规范

- **API 请求**：统一通过 `@/utils/request.ts` 中的 axios 实例发起，不要直接使用 `fetch`
- **Token 管理**：通过 `@/utils/auth.ts` 中的 `getToken` / `setToken` / `removeToken` 操作，Key 为 `Token`
- **状态管理**：按功能域在 `stores/` 下创建 Pinia Store
- **样式**：优先使用 Tailwind 原子类（`tw-` 前缀），Element Plus 组件样式通过全局变量覆盖
- **路由**：新业务模块在 `router/modules/` 下定义，通过 `router.addRoute()` 动态注册
- **分页**：所有列表页使用 `el-pagination`，layout 统一 `"total, sizes, prev, pager, next"`，默认每页 20 条，分页容器水平居中（`tw-justify-center`），全局已注册中文语言包
- **国际化**：Element Plus 已配置中文 locale（`element-plus/dist/locale/zh-cn.mjs`），分页/表格等组件默认显示中文

## 登录

### 模式切换

项目根据运行环境自动切换登录方式：

```
isLocalDev() ?
  ├─ true  → 本地开发：显示手动登录表单（用户名 + 密码）
  └─ false → 测试/生产：自动跳转 CAS 统一登录
```

### 本地开发（手动登录）

```
访问 /login → 显示 Soft UI 登录表单
  → 输入用户名 + 密码 → 点击「登录」
  → GET /qdm-b2b/api/oauth2/unifty/token
      ?client_id=default
      &client_secret=default_sec
      &grant_type=password
      &username=<用户输入>
      &password=<用户输入>
      &scope=all
  → 成功 → 存 Token → 跳转 /home
```

### 测试/生产（CAS 统一登录）

```
用户访问任何受保护页面
  → 路由守卫检测 Token
    → 无 Token → 跳转 /login → 自动重定向到 CAS 登录页
    → CAS 登录成功 → 回调 /home?ticket=ST-xxx
  → 路由守卫拦截 ticket → 调用 loginByTicket() 换取 Token
    → GET /qdm-b2b/api/oauth2/unifty/token
        ?client_id=cas
        &client_secret=cas_sec
        &grant_type=ticket
        &st=<ticket>
        &service=<回调地址>
        &scope=all
  → 成功 → 存入 localStorage → 跳转 /home

Token 失效时（API 返回 401）：
  → 清除本地 Token → 自动跳转 CAS 登录页 → 登录后回到 /home
```

### 防死循环机制

CAS Ticket 换 Token 失败时，内置防死循环保护：

```
loginByTicket 失败
  → retryCount +1 写入 sessionStorage
  → retryCount < 3：等待 500ms → 重新加载当前 URL（使用新 ticket 重试）
  → retryCount ≥ 3：清除计数 → 跳转 /login?error=ticket_failed（停止跳转，显示错误页）
```

### 环境对照

| 环境 | CAS 地址 | 回调地址 | API Base |
|------|---------|----------|----------|
| 本地开发 | `cas-qa.qdama.cn` | `http://localhost:8080/home` | `b2b-buy-qa.qdama.cn/qdm-b2b/api` |
| 测试 | `cas-qa.qdama.cn` | `https://b2b-buy-qa.qdama.cn/home` | `b2b-buy-qa.qdama.cn/qdm-b2b/api` |
| 生产 | `cas.qdama.cn` | `https://b2b-buy.qdama.cn/home` | `b2b-buy.qdama.cn/qdm-b2b/api` |

### 登录 API 参数

| 场景 | 请求方式 | client_id | client_secret | grant_type | 额外参数 |
|------|---------|-----------|---------------|------------|---------|
| 本地手动登录 | `GET` | `default` | `default_sec` | `password` | `username`, `password` |
| CAS Ticket 登录 | `GET` | `cas` | `cas_sec` | `ticket` | `st`, `service` |

## 单元测试

### 技术栈

| 组件 | 版本 | 作用 |
|------|------|------|
| Vitest | ^4.1.9 | 测试运行器 + 断言库 |
| @vue/test-utils | ^2.4.11 | Vue 组件 mount / 交互测试 |
| @testing-library/vue | ^8.1.0 | 用户视角 DOM 查询 / cleanup |
| jsdom | ^29.1.1 | 模拟浏览器 DOM 环境 |

### 全局 Setup（src/test/setup.ts）

所有测试文件自动注入以下基础设施，**无需手动注册**：

- **Element Plus 全部组件** — `ElButton`、`ElInput`、`ElMessage` 等直接可用
- **@element-plus/icons-vue 全部图标** — `<el-icon><Edit /></el-icon>` 正常渲染
- **ElMessage Mock** — `success` / `error` / `warning` / `info` 均为 `vi.fn()`，不抛 DOM 错误
- **@testing-library/vue cleanup** — 每个测试后自动 `cleanup()`
- **Pinia 辅助导出** — `createPinia` / `setActivePinia` 从 setup 直接导入

### 测试脚本

| 命令 | 说明 |
|------|------|
| `pnpm test` | 启动 Vitest watch 模式（开发时实时重跑） |
| `pnpm test:run` | 单次运行全部测试（CI 用） |
| `pnpm test:coverage` | 运行测试并生成覆盖率报告 |

### 编写测试示例

#### 基础组件测试（@vue/test-utils）

```ts
// src/stores/__tests__/useCounter.test.ts
import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from '@/test/setup'
import Counter from './Counter.vue'

describe('Counter.vue', () => {
  beforeEach(() => {
    // 每个测试前激活全新 Pinia 实例
    setActivePinia(createPinia())
  })

  it('渲染 ElButton 并响应点击', async () => {
    const wrapper = mount(Counter, {
      props: { initial: 0 },
    })

    const btn = wrapper.findComponent({ name: 'ElButton' })
    expect(btn.exists()).toBe(true)

    await btn.trigger('click')
    expect(wrapper.text()).toContain('1')
  })
})
```

#### 组件 DOM 查询测试（@testing-library/vue）

```ts
// src/views/__tests__/HomeView.test.ts
import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/vue'
import { createPinia, setActivePinia } from '@/test/setup'
import HomeView from '@/views/home/index.vue'

describe('HomeView', () => {
  it('渲染首页欢迎内容', () => {
    setActivePinia(createPinia())

    render(HomeView)

    expect(screen.getByText('欢迎使用管理后台')).toBeDefined()
  })
})
```

#### API 层测试（Mock axios）

```ts
// src/api/__tests__/auth.test.ts
import { describe, it, expect, vi } from 'vitest'

// Mock axios 实例
vi.mock('@/utils/request', () => ({
  default: {
    get: vi.fn().mockResolvedValue({
      code: '200',
      data: {
        access_token: 'test-token',
        real_name: '测试用户',
        mobile: '13800138000',
      },
    }),
  },
}))

describe('loginByPassword', () => {
  it('返回 access_token 和用户信息', async () => {
    const { loginByPassword } = await import('@/api/auth')
    const result = await loginByPassword('admin', '123456')

    expect(result.access_token).toBe('test-token')
    expect(result.real_name).toBe('测试用户')
    expect(result.mobile).toBe('13800138000')
  })
})
```

#### Pinia Store 测试

```ts
// src/stores/__tests__/useAuth.test.ts
import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from '@/test/setup'
import { useAuthStore } from '@/stores/useAuth'

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('初始状态 token 为空', () => {
    const store = useAuthStore()
    expect(store.token).toBe('')
  })

  it('login 后 token 正确赋值', () => {
    const store = useAuthStore()
    store.login('mock-token-xyz')
    expect(store.token).toBe('mock-token-xyz')
  })
})
```

### 运行测试

```bash
# 单次运行
pnpm test:run

# Watch 模式（开发时推荐）
pnpm test
```

## 阿里云 SLS 日志监控

项目已集成阿里云 SLS 日志服务，接口请求失败时自动上报错误日志。

- **日志封装**：[src/utils/sls.ts](src/utils/sls.ts) — 单例 Tracker + STS 凭证自动刷新
- **上报时机**：HTTP 响应拦截器中，业务错误（code !== 200，兼容字符串和数字类型）、HTTP 错误（4xx/5xx）、网络异常均自动调用 `sendSlsLog()`
- **上报字段**：`system`（默认 `tob-WEB`）、`url`、`statusCode`、`errorResponseBody`、`requestTime`、`userId`、`version`、`deviceType`、`terminalType` 等
- **手动上报**：
  ```ts
  import { sendSlsLog } from '@/utils/sls'
  sendSlsLog({ url: '/api/xxx', statusCode: '500', errorResponseBody: '...' })
  ```

## UI 设计体系

采用 **UI/UX Pro Max** 设计工具，选定 **Soft UI Evolution + AI-Native UI** 风格：

| 维度 | 选择 |
|------|------|
| 风格 | Soft UI Evolution + AI-Native UI — 柔和多层阴影 + 极简 AI 交互 |
| 主色 | `#409eff` |
| 辅助色 | `#79bbff` |
| 背景 | `#F8FAFC` 冷灰白 |
| 文字 | `#1E293B` (Slate-800) |
| 标题字体 | Varela Round — 圆润友好 |
| 正文字体 | Nunito Sans — 柔和温暖 |
| 品牌 | **钱鲜达** — 生鲜供应链智能管理平台 |
| 卡片圆角 | 10px (input) / 12px (button) / 14px (card) / 20px (chip) |
| 阴影 | 多层柔和（`0 4px 24px rgba(0,0,0,0.05)`） |
| 动画 | 200-300ms ease |

## 构建与发布

### 环境配置

| 环境 | 构建命令 | API 地址 | CAS 地址 | 输出目录 |
|------|----------|----------|----------|----------|
| 开发 | `pnpm dev` | `.env.development` | cas-qa | - |
| 测试 | `pnpm qa` | `.env.test` | cas-qa | `dist-qa/` |
| 生产 | `pnpm pro` | `.env.production` | cas | `dist-pro/` |

### 可用脚本

| 命令 | 说明 |
|------|------|
| `pnpm dev` | 启动开发服务器 |
| `pnpm build` | 类型检查 + 生产构建（输出 `dist/`） |
| `pnpm build:only` | 仅 Vite 构建，跳过 vue-tsc（OOM 时使用） |
| `pnpm qa` | 类型检查 + 测试构建（输出 `dist-qa/`） |
| `pnpm pro` | 类型检查 + 生产构建（输出 `dist-pro/`） |
| `pnpm preview` | 本地预览构建产物 |
| `pnpm test` | 启动 Vitest Watch 模式 |
| `pnpm test:run` | 单次运行全部测试 |
| `pnpm test:coverage` | 运行测试 + 生成覆盖率报告 |
| `pnpm lint` | ESLint 检查 + 自动修复 |
| `pnpm format` | Prettier 格式化 |

### 发布流程

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────┐
│ 1. 终端构建   │ ──▶ │ 2. 本地预览验证   │ ──▶ │ 3. 部署服务器  │
│    pnpm qa/pro│     │    pnpm preview   │     │    dist-qa/    │
└──────────────┘     └──────────────────┘     │    dist-pro/   │
                                               └──────────────┘
```

**详细步骤：**

1. **终端构建**
   ```bash
   # 测试环境
   pnpm qa

   # 生产环境
   pnpm pro
   ```

2. **本地预览验证**
   ```bash
   pnpm preview
   ```

3. **部署**
   - 将 `dist-pro/` 或 `dist-qa/` 目录部署到对应服务器
   - 确保 Web 服务器（Nginx/Apache）配置了 SPA 路由回退

### Nginx 配置参考

```nginx
server {
    listen 80;
    server_name example.com;

    root /var/www/tob-web;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

## 常见问题

### 登录后一直刷新 Ticket 无法进入首页

检查 API 地址是否正确。登录接口为 `GET /qdm-b2b/api/oauth2/unifty/token`。路由守卫内置防死循环机制：连续 3 次 ticket 验证失败后停止自动跳转，显示错误页。

### 手动登录显示"成功"后又显示"登录失败"

检查 API 返回的 `code` 字段类型。拦截器已兼容字符串 `"200"` 和数字 `200`，`real_name` 直接使用 API 返回值（不进行 `decodeURIComponent`）。

### 测试报 "找不到 ElButton" 或 "Pinia 未激活"

确认 `vite.config.ts` 中 `test.setupFiles` 配置正确指向 `./src/test/setup.ts`。setup 文件已自动注册 Element Plus 全部组件和 Pinia 辅助函数。

### 构建 OOM（内存溢出）

使用 `pnpm build:only` 跳过 `vue-tsc` 类型检查，仅执行 Vite 打包。

### Tailwind 样式不生效

1. 检查是否使用了 `tw-` 前缀（项目配置 `prefix: 'tw-'`），如 `tw-flex`、`tw-p-4`
2. 检查 `tailwind.config.js` 中 `content` 路径是否包含目标文件

### 修改环境变量后不生效

修改 `.env.*` 文件后需要**重启 dev server** 或**重新构建**，环境变量在启动时注入。
