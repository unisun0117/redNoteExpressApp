---
paths:
  - tob-frontend/tob-frontend-mobile/**
description: TOB 微信小程序开发规范，适用于 tob-frontend-mobile 项目
alwaysApply: true
---

> **设计系统**：所有 UI 开发必须遵守下方 ui-ux-pro-max 设计规范。
> 开发任何页面/组件前，先对照 MASTER.md 的色彩、字体、圆角、阴影规范，禁止偏离。

# TOB 微信小程序（tob-frontend-mobile）

## 技术栈

uni-app (Vue 3) + TypeScript · Vite · pnpm · uview-plus
Tailwind CSS（无前缀，标准类名） · Pinia（`store/modules/` 按功能域拆分）
luch-request（`src/api/request.ts`，导出 `http` 实例） · 阿里云 SLS（`@aliyun-sls/web-track-mini`）
Vitest + @vue/test-utils + jsdom

## 代码规范

- **Prettier**：无分号 + 单引号 + tab 宽度 2 + 尾逗号 all + 行宽 100 + 箭头函数括号 always + LF 换行
- **ESLint**：`eslint:recommended` + `plugin:vue/vue3-recommended` + `@typescript-eslint/recommended` + `plugin:prettier/recommended`
- **TS 安全**：禁止 `any` / `ts-ignore` / `ts-expect-error`；API 返回必须有泛型接口；未使用变量 `_` 前缀
- 路径别名：`@/` → `src/`

## 目录分层

```
src/
├── api/modules/    # API 按业务模块拆分，index.ts 统一导出
├── pages/          # 主包页面
├── sub-pages/      # 分包页面（order/, goods/, user/, setting/）
├── store/modules/  # Pinia Store，按功能域创建
├── styles/         # 全局样式 + Tailwind 入口
├── utils/          # env.ts, sls.ts, index.ts
├── static/         # 静态资源（tabbar 图标等）
└── __tests__/      # 测试文件
```

## 路由

- 由 `pages.json` 统一管理，**禁止手动创建路由实例**
- 主包页面放 `pages/`，分包页面放 `sub-pages/<模块>/pages/`
- 新增分包必须在 `pages.json` 注册 `subPackages`
- TabBar 页面 2-5 个，icon 放 `static/` 目录

## API 请求

- 统一入口：`src/api/request.ts`（luch-request），导出 `http` 实例
- **禁止直接使用 `uni.request`**
- 请求拦截器：注入 `Authorization: Bearer <token>` + `requestId` + loading 状态
- 响应拦截器：`code === 0` 或 `code === 200` 成功；401 `reLaunch` 到 `/pages/login/index`；其余 `uni.showToast`
- 支持 `showLoading` / `skipAuth` 配置

## Token

- `uni.getStorageSync('token')` / `uni.setStorageSync`
- 401 时清除 token，`uni.reLaunch({ url: '/pages/login/index' })`

## 状态管理

- Pinia Store 放 `store/modules/`，按功能域创建
- 禁止操作 DOM/路由，异步必须处理 loading/error

## UI/UX Pro Max 设计约束

| 维度 | 值 |
|------|----|
| **风格** | Restaurant & Food — 食欲红 + 暖金，温暖烟火气 |
| **主色** | `#DC2626` (Red-600) |
| **辅助色** | `#F87171` (Red-400) |
| **CTA 色** | `#CA8A04` (Amber-500) 暖金（同一视图最多 1 个） |
| **背景** | `#FEF2F2 → #FFF7ED → #FFFFFF` 暖色渐变 |
| **文字主色** | `#450A0A` (Red-950) |
| **标题字体** | Serif |
| **正文字体** | Sans |

- **禁止**偏离上述体系自行定义样式
- **禁止**混用 web 的 Soft UI / Emerald 绿色系
- **动画**：200-300ms，ease/ease-in-out，禁止 <150ms 或 >500ms

## 样式

- Tailwind 优先（无前缀，标准类名）
- uview-plus 通过 CSS 变量覆盖

## 小程序分包

- 主包：2-3 个页面（首页 + 登录 + TabBar）
- 其余业务拆分到 `sub-pages/`
- 构建产物：`dist/dev/mp-weixin`（开发，未压缩，有 source map）/ `dist/build/mp-weixin`（发布，已压缩，无 source map）
- **上传须用 `dist/build/mp-weixin`**，不推荐用 dev 产物（体积大、暴露源码）
- 发布流程：`pnpm build:mp-weixin:prod` → 微信开发者工具导入 build 目录 → 验证 → 上传 → 微信后台提交审核

## 测试

- `pnpm test`（交互 watch）/ `pnpm test:run`（CI 单次）/ `pnpm test:coverage` / `pnpm test:ui`
- 全局 setup 覆盖：Pinia 实例 + uni-app API mock + 浏览器 API mock

## SLS 日志

- `src/utils/sls.ts` 封装（`@aliyun-sls/web-track-mini`），`generateRequestId()` 生成 UUID v4
- 响应拦截器自动上报（`system`: tob-小程序）
- 上报字段：`url` / `statusCode` / `errorResponseBody` / `requestTime` / `userId` / `version`

## 环境

`.env.development` → `.env.test` → `.env.production`，变量以 `VITE_` 开头，修改后重新构建

## 构建

| 环境 | 命令 | 输出 |
|------|------|------|
| 开发 | `pnpm dev:mp-weixin` | `dist/dev/mp-weixin` |
| 测试 | `pnpm build:mp-weixin:test` | `dist/build/mp-weixin` |
| 生产 | `pnpm build:mp-weixin:prod` | `dist/build/mp-weixin` |

## 富文本编辑器 Tiptap

- 官方中文文档：https://tiptap.zhcndoc.com
- 已安装：`@tiptap/vue-3` `@tiptap/starter-kit`
- 禁止使用其他富文本方案
- 小程序环境限制：ProseMirror 依赖 DOM API，复杂扩展（Image/Link）可能不兼容，建议只用 starter-kit 基础功能
- **禁止**安装 `@tiptap-pro/` 前缀的付费扩展包

## OSS 文件上传

- 已封装 `src/utils/oss.ts` — 基于 `ali-oss` + STS 直传，适配小程序文件路径（tempFilePath）
- 环境变量同上
- 小程序用法：`ossUpload(tempFilePath)` 接收 `wx.chooseImage` 返回的临时路径，非 Blob/File 对象
# Design System Master File

> **LOGIC:** When building a specific page, first check `design-system/pages/[page-name].md`.
> If that file exists, its rules **override** this Master file.
> If not, strictly follow the rules below.

---

**Project:** TOB-Mobile
**Generated:** 2026-07-09 18:25:52
**Category:** Airline

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

**Style:** Exaggerated Minimalism

**Keywords:** Bold minimalism, oversized typography, high contrast, negative space, loud minimal, statement design

**Best For:** Fashion, architecture, portfolios, agency landing pages, luxury brands, editorial

**Key Effects:** font-size: clamp(3rem 10vw 12rem), font-weight: 900, letter-spacing: -0.05em, massive whitespace

### Page Pattern

**Pattern Name:** Waitlist/Coming Soon

- **Conversion Strategy:** Scarcity + exclusivity. Show waitlist count. Early access benefits. Referral program.
- **CTA Placement:** Email form prominent (above fold) + Sticky form on scroll
- **Section Order:** 1. Hero with countdown, 2. Product teaser/preview, 3. Email capture form, 4. Social proof (waitlist count)

---

## Anti-Patterns (Do NOT Use)

- ❌ Complex booking
- ❌ Poor mobile

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
