---
paths:
  - tob-frontend/tob-frontend-h5/**
description: TOB 企业微信 H5 移动端开发规范，适用于 tob-frontend-h5 项目
alwaysApply: true
---

> **设计系统**：所有 UI 开发必须遵守下方 ui-ux-pro-max 设计规范。
> 开发任何页面/组件前，先对照 MASTER.md 的色彩、字体、圆角、阴影规范，禁止偏离。

# TOB 企业微信 H5（tob-frontend-h5）

## 技术栈

Vue 3 Composition API + TypeScript（禁止 Options API） · Vite · pnpm · Vant 4 · Element Plus（仅测试/管理后台用）
Tailwind CSS（无前缀，标准类名） · Pinia · Vue Router (History 模式) · Axios（`src/utils/axios.ts`，导出 `get/post/put/del`）
weixin-js-sdk · 阿里云 SLS · Vitest + @vue/test-utils + @testing-library/vue + jsdom

## 代码规范

- **Prettier**：无分号 + 单引号 + tab 宽度 2 + 尾逗号 all + 行宽 100 + 箭头函数括号 always + LF 换行
- **ESLint**：`eslint:recommended` + `plugin:vue/vue3-recommended` + `@typescript-eslint/recommended` + `plugin:prettier/recommended`
- **TS 安全**：禁止 `any` / `ts-ignore` / `ts-expect-error`；API 返回必须有泛型接口；未使用变量 `_` 前缀
- 路径别名：`@/` → `src/`

## 目录分层

```
src/
├── router/      # 路由，businessRoutes 数组动态注册
├── store/       # Pinia Store，按功能域创建
├── styles/      # 全局样式 + Tailwind 入口
├── utils/       # axios.ts, sls.ts, wechat.ts
├── types/       # 全局类型声明（wechat.d.ts 等）
├── views/       # 页面组件
└── __tests__/   # 测试文件
```

## 企业微信集成

- **所有企微 API 必须通过 `src/utils/wechat.ts` 封装调用**，禁止业务代码直接操作 `wx.*`
- 非企业微信环境自动静默跳过 SDK 初始化（不影响普通浏览器访问）
- 鉴权流程：`App.vue onMounted → initWxConfig() → wx.config → wx.ready/wx.error`
- Token 仅用 `sessionStorage`（关闭浏览器即清除），Key = `token`
- 401/403 时清除 token + userInfo，跳转 `/login`
- 预置 JS 接口：`hideOptionMenu` / `closeWindow` / `scanQRCode` / `previewImage` / `getLocation` / `getNetworkType` 等
- WWOpenData 类型声明已完整覆盖

## API 请求

- 统一入口：`src/utils/axios.ts`，导出 `get`/`post`/`put`/`del`（禁止直接 fetch）
- 请求拦截器：注入 `Authorization: Bearer <token>` + 企微 `X-WX-Corp-ID` / `X-WX-Agent-ID` + `requestId`
- 响应拦截器：`code === 0` 或 `code === 200` 成功；401/403 跳登录；其余 `showToast`
- 支持 `skipErrorToast` 跳过自动提示

## 路由

- `lazyView()` 懒加载辅助函数
- 基础路由在主文件定义，业务路由 push 到 `businessRoutes` 数组动态注册
- 全局守卫：Token 校验 → 页面标题设置

## 状态管理

- Pinia Store 放 `store/`，按功能域创建
- 禁止操作 DOM/路由，异步必须处理 loading/error

## UI/UX Pro Max 设计约束

| 维度 | 值 |
|------|----|
| **风格** | Restaurant & Food — 食欲红 + 暖金，温暖烟火气 |
| **主色** | `#DC2626` (Red-600) |
| **辅助色** | `#F87171` (Red-400) |
| **CTA 色** | `#CA8A04` (Amber-600) 暖金（同一视图最多 1 个） |
| **背景** | `#FEF2F2 → #FFF7ED → #FFFFFF` 暖色渐变（禁止反方向/冷色调） |
| **文字主色** | `#450A0A` (Red-950) |
| **文字辅助** | `#7F1D1D` (Red-900) |
| **标题字体** | Serif（Noto Serif SC / Source Han Serif CN） |
| **正文字体** | Sans（Nunito Sans / Inter / PingFang SC） |
| **Card 圆角** | `12px` |
| **Button 圆角** | `8px` |
| **Input 圆角** | `6px` |
| **阴影** | `0 2px 16px rgba(220,38,38,0.08)`（仅卡片/弹窗/下拉菜单） |

- **禁止**偏离上述体系自行定义样式、写死 HEX 值
- **禁止**混用 web 的 Soft UI / Emerald 绿色系
- **动画**：200-300ms，ease/ease-in-out，禁止 <150ms 或 >500ms，禁止瞬间跳变
- Tailwind 主题色通过 `theme.extend.colors.brand` 注入

## 样式

- Tailwind 优先（无前缀，标准类名）
- Vant 4 样式通过 CSS 变量覆盖
- PostCSS `postcss-pxtorem` 自动 px→rem（设计稿 375px，rootValue: 37.5）
- 禁止转换加 `.norem` 类名
- viewport：`user-scalable=no, viewport-fit=cover`

## 测试

- Vitest（`pnpm test`）/ `pnpm test:run`（CI）/ `pnpm test:coverage` / `pnpm test:ui` / `pnpm test:ts`（类型检查）
- 全局 setup 覆盖：Element Plus 组件 + Pinia 实例 + 浏览器 API mock

## SLS 日志

- `src/utils/sls.ts` 封装，`generateRequestId()` 生成 UUID v4
- 响应拦截器自动上报错误（`system`: tob-WEB，字段同 web）
- 本地 `VITE_SLS_ENABLED=false`

## 环境

`.env.development` → `.env.test` → `.env.production`，变量以 `VITE_` 开头，修改后重启 dev server
额外字段：`VITE_WX_CORP_ID` / `VITE_WX_AGENT_ID`

## 构建与部署

| 环境 | 命令 | 输出 |
|------|------|------|
| 开发 | `pnpm dev` | - |
| 测试 | `pnpm build:test` | `dist-qa/` |
| 生产 | `pnpm build:prod` | `dist-pro/` |

- 构建后 `pnpm preview` 本地验证
- Nginx：`try_files $uri $uri/ /index.html`，`/assets/` 强缓存 1y

## 富文本编辑器 Tiptap

- 官方中文文档：https://tiptap.zhcndoc.com
- 已安装：`@tiptap/vue-3` `@tiptap/starter-kit` `@tiptap/extension-placeholder` `@tiptap/extension-image` `@tiptap/extension-link`
- 禁止使用其他富文本方案
- 用法同上（tob-frontend-web）
- **禁止**安装 `@tiptap-pro/` 前缀的付费扩展包

## OSS 文件上传

- 已封装 `src/utils/oss.ts` — 与 tob-frontend-web 相同，基于 `ali-oss` + STS 直传
- 环境变量同上，用法同上
# Design System Master File

> **LOGIC:** When building a specific page, first check `design-system/pages/[page-name].md`.
> If that file exists, its rules **override** this Master file.
> If not, strictly follow the rules below.

---

**Project:** TOB-H5
**Generated:** 2026-07-09 18:25:30
**Category:** Restaurant/Food Service

---

## Global Rules

### Color Palette

| Role | Hex | CSS Variable |
|------|-----|--------------|
| Primary | `#DC2626` | `--color-primary` |
| Secondary | `#F87171` | `--color-secondary` |
| CTA/Accent | `#CA8A04` | `--color-cta` |
| Background | `#FEF2F2` | `--color-background` |
| Text | `#450A0A` | `--color-text` |

**Color Notes:** Appetizing red + warm gold

### Typography

- **Heading Font:** Playfair Display SC
- **Body Font:** Karla
- **Mood:** restaurant, menu, culinary, elegant, foodie, hospitality
- **Google Fonts:** [Playfair Display SC + Karla](https://fonts.google.com/share?selection.family=Karla:wght@300;400;500;600;700|Playfair+Display+SC:wght@400;700)

**CSS Import:**
```css
@import url('https://fonts.googleapis.com/css2?family=Karla:wght@300;400;500;600;700&family=Playfair+Display+SC:wght@400;700&display=swap');
```

### Spacing Variables

| Token | Value | Usage |
|-------|-------|-------|
| `--space-xs` | `4px` / `0.25rem` | Tight gaps |
| `--space-sm` | `8px` / `0.5rem` | Icon gaps, inline spacing |
| `--space-md` | `16px` / `1rem` | Standard padding |
| `--space-lg` | `24px` / `1.5rem` | Section padding |
| `--space-xl` | `32px` / `2rem` | Large gaps |
| `--space-2xl` | `48px` / `3rem` | Section margins |
| `--space-3xl` | `64px` / `4rem` | Hero padding |

### Shadow Depths

| Level | Value | Usage |
|-------|-------|-------|
| `--shadow-sm` | `0 1px 2px rgba(0,0,0,0.05)` | Subtle lift |
| `--shadow-md` | `0 4px 6px rgba(0,0,0,0.1)` | Cards, buttons |
| `--shadow-lg` | `0 10px 15px rgba(0,0,0,0.1)` | Modals, dropdowns |
| `--shadow-xl` | `0 20px 25px rgba(0,0,0,0.15)` | Hero images, featured cards |

---

## Component Specs

### Buttons

```css
/* Primary Button */
.btn-primary {
  background: #CA8A04;
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 200ms ease;
  cursor: pointer;
}

.btn-primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* Secondary Button */
.btn-secondary {
  background: transparent;
  color: #DC2626;
  border: 2px solid #DC2626;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 200ms ease;
  cursor: pointer;
}
```

### Cards

```css
.card {
  background: #FEF2F2;
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--shadow-md);
  transition: all 200ms ease;
  cursor: pointer;
}

.card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}
```

### Inputs

```css
.input {
  padding: 12px 16px;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 200ms ease;
}

.input:focus {
  border-color: #DC2626;
  outline: none;
  box-shadow: 0 0 0 3px #DC262620;
}
```

### Modals

```css
.modal-overlay {
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.modal {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: var(--shadow-xl);
  max-width: 500px;
  width: 90%;
}
```

---

## Style Guidelines

**Style:** Vibrant & Block-based

**Keywords:** Bold, energetic, playful, block layout, geometric shapes, high color contrast, duotone, modern, energetic

**Best For:** Startups, creative agencies, gaming, social media, youth-focused, entertainment, consumer

**Key Effects:** Large sections (48px+ gaps), animated patterns, bold hover (color shift), scroll-snap, large type (32px+), 200-300ms

### Page Pattern

**Pattern Name:** Enterprise Gateway

- **Conversion Strategy:**  logo carousel,  tab switching for industries, Path selection (I am a...). Mega menu navigation. Trust signals prominent.
- **CTA Placement:** Contact Sales (Primary) + Login (Secondary)
- **Section Order:** 1. Hero (Video/Mission), 2. Solutions by Industry, 3. Solutions by Role, 4. Client Logos, 5. Contact Sales

---

## Anti-Patterns (Do NOT Use)

- ❌ Low-quality imagery
- ❌ Outdated hours

### Additional Forbidden Patterns

- ❌ **Emojis as icons** — Use SVG icons (Heroicons, Lucide, Simple Icons)
- ❌ **Missing cursor:pointer** — All clickable elements must have cursor:pointer
- ❌ **Layout-shifting hovers** — Avoid scale transforms that shift layout
- ❌ **Low contrast text** — Maintain 4.5:1 minimum contrast ratio
- ❌ **Instant state changes** — Always use transitions (150-300ms)
- ❌ **Invisible focus states** — Focus states must be visible for a11y

---

## Pre-Delivery Checklist

Before delivering any UI code, verify:

- [ ] No emojis used as icons (use SVG instead)
- [ ] All icons from consistent icon set (Heroicons/Lucide)
- [ ] `cursor-pointer` on all clickable elements
- [ ] Hover states with smooth transitions (150-300ms)
- [ ] Light mode: text contrast 4.5:1 minimum
- [ ] Focus states visible for keyboard navigation
- [ ] `prefers-reduced-motion` respected
- [ ] Responsive: 375px, 768px, 1024px, 1440px
- [ ] No content hidden behind fixed navbars
- [ ] No horizontal scroll on mobile
