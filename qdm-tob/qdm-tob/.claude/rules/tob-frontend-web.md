---
paths:
  - tob-frontend/tob-frontend-web/**
description: TOB PC 端管理后台开发规范，适用于 tob-frontend-web 项目
alwaysApply: true
---

> **设计系统**：所有 UI 开发必须遵守下方 ui-ux-pro-max 设计规范。
> 开发任何页面/组件前，先对照 MASTER.md 的色彩、字体、圆角、阴影规范，禁止偏离。

# TOB PC 端管理后台（tob-frontend-web）

## 技术栈

Vue 3 Composition API + TypeScript（禁止 Options API） · Vite · pnpm · Element Plus + @element-plus/icons-vue（已全局注册）
Tailwind CSS（**`tw-` 前缀**，如 `tw-flex`、`tw-p-4`） · Pinia · Vue Router (History 模式) · Axios · Vitest + @vue/test-utils + @testing-library/vue + jsdom

## 代码规范

- **Prettier**：无分号 + 单引号 + tab 宽度 2 + 尾逗号 all + 行宽 100 + 箭头函数括号 always + LF 换行
- **ESLint**：`eslint:recommended` + `plugin:vue/vue3-recommended` + `@typescript-eslint/recommended` + `plugin:prettier/recommended`
- **TS 安全**：禁止 `any` / `ts-ignore` / `ts-expect-error`；API 返回必须有泛型接口；未使用变量 `_` 前缀
- 路径别名：`@/` → `src/`

## 目录分层

```
src/
├── api/modules/       # API 按业务模块拆分，index.ts 统一导出
├── router/modules/    # 业务路由，导出 RouteRecordRaw[]，addRoute 动态注册
├── stores/            # Pinia Store，按功能域创建
├── views/<module>/    # 页面组件（路由入口 + 业务组装）
├── layouts/           # 布局组件（框架，不写业务逻辑）
├── components/        # 公共组件（禁止直接调 API）
├── utils/             # request.ts, auth.ts, sls.ts, chinaArea.ts
├── styles/            # 全局样式 + Tailwind 入口 + CSS 变量
└── __tests__/         # 测试文件
```

### 公用工具

| 工具 | 路径 | 用途 |
|------|------|------|
| **省市区数据** | `@/utils/chinaArea` | 基于 `element-china-area-data`，导出 `chinaAreaOptions` 供 el-cascader 使用 |

- 所有需要省/市/区级联选择的页面统一 `import { chinaAreaOptions } from '@/utils/chinaArea'`
- 禁止在组件内硬编码地区选项
- `element-china-area-data` 已全局安装，无需重复安装

## 登录

本地开发：手动表单（用户名+密码） → GET `/oauth2/unifty/token?grant_type=password`
测试/生产：CAS 统一登录 → 路由守卫检测 Token → 无则跳 CAS → 回调带 ticket → 换 Token → 存 localStorage（Key = `Token`）
CAS 防死循环：ticket 换 token 连续失败 ≥3 次停止自动跳转

## API 请求

- 统一入口：`src/utils/request.ts`（禁止直接 fetch）
- 请求拦截器：注入 `Authorization: Bearer <token>` + 生成 `requestId` header
- 响应拦截器：`code === 0` 或 `code === 200` 成功；401 跳 CAS；其余 `ElMessage.error`
- API 函数必须有明确泛型类型

## Token

- `localStorage`，Key = `Token`
- 通过 `src/utils/auth.ts` 的 `getToken`/`setToken`/`clearAuth` 操作，禁止直接操作 localStorage
- 退出登录同时清除 Token 和用户信息

## 路由

- `router/modules/` 下创建模块文件 `RouteRecordRaw[]`，主路由 `addRoute` 动态注册
- 全局守卫：CAS Ticket 处理 → Token 校验 → 页面标题设置
- **新增模块必须在此表注册路由，禁止遗漏**

### 项目模块目录

| 模块名称 | 路由地址 |
|---------|---------|
| 首页 | `/home` |
| 登录 | `/login` |
| 运营管理 → 销售大区 | `/operation/sales-region` |
| 运营管理 → 销售员管理 | `/operation/salesman` |
| 运营管理 → 仓库管理 | `/operation/warehouse` |
| 运营管理 → 公告管理 | `/operation/announcement` |
| 系统管理 → 用户管理 | `/system/user` |
| 系统管理 → 菜单管理 | `/system/menu` |
| 系统管理 → 角色管理 | `/system/role` |
| 系统管理 → 字典管理 | `/system/dict` |
| 系统管理 → 小程序隐私政策 | `/system/privacy-policy` |
| 客户管理 → 登录账号 | `/customer/customer-account` |
| 客户管理 → 客户档案 | `/customer/customer-archive` |
| 商品管理 → 商品分类 | `/product/category` |
| 商品管理 → 商品资料 | `/product/catalog` |
| 商品管理 → 商品基础资料 | `/product/list` |
| 商品管理 → 商品价格管理 | `/product/price` |

> ⚠️ **强制规则**：新增模块时必须在 `router/modules/` 创建路由文件，并**同步更新本表**。
> 格式：「一级菜单 → 二级菜单，`/路由地址`」。遗漏视为未完成开发。

## 状态管理

- Pinia Store 放 `stores/`，按功能域创建
- 禁止操作 DOM/路由，异步必须处理 loading/error

## UI/UX Pro Max 设计约束

> **风格来源**：`ui-ux-pro-max` Skill 生成（Data-Dense Dashboard），已合并到本文末尾。
> **自动生效**：此规则文件处理 `tob-frontend-web/**` 时自动注入上下文。样式修改前对照本表，禁止偏离。

| 维度 | 值 |
|------|----|
| **风格** | Data-Dense Dashboard — 数据密集型仪表盘，KPI 卡片 + 数据表格 + 最小内边距 |
| **主色** | `#0369A1` (Sky-700) |
| **辅助色** | `#334155` (Slate-700) |
| **侧边栏** | `#304156` |
| **背景** | `#F8FAFC` 冷灰白 |
| **文字主色** | `#0F172A` (Slate-900) |
| **文字辅助** | `#475569` (Slate-600) |
| **标题字体** | Fira Code |
| **正文字体** | Fira Sans |
| **Card 圆角** | `14px` |
| **Button 圆角** | `12px` |
| **Input 圆角** | `10px` |
| **Chip 圆角** | `20px` |
| **阴影** | `0 4px 24px rgba(0,0,0,0.05)`（仅卡片/弹窗/下拉菜单） |

- **禁止**偏离上述体系自行定义样式、写死 HEX 值，必须用 Tailwind 主题色或 CSS 变量
- **禁止**混用 h5/mobile 的 Restaurant & Food 色系
- **动画**：200-300ms，ease/ease-in-out，禁止 <150ms 或 >500ms，禁止瞬间跳变

## 样式

- Tailwind 优先，所有类必须带 `tw-` 前缀
- Element Plus 主题通过 CSS 变量覆盖（`styles/index.css`）
- CSS 变量：`--color-primary: #0369A1`、`--bg-color: #F8FAFC`、`--text-color: #0F172A`
- 阴影仅用于卡片、弹窗、下拉等浮层元素，禁止正文/按钮/输入框

## 分页

```html
<div class="tw-mt-4 tw-flex">
  <el-pagination
    v-model:current-page="pageNum" v-model:page-size="pageSize"
    :page-sizes="[10, 20, 50]" :total="total"
    layout="total, sizes, prev, pager, next" background />
</div>
```
- 默认 20 条，水平居中，Element Plus 已注册中文 locale（禁止英文文案）

## 弹窗/Overlay

- `styles/index.css` 已全局重置 `.el-overlay` / `.el-dialog` 的 `margin-top`
- 所有 `el-dialog` 必须加 `align-center`
- 禁止页面级 Tailwind `space-y-*` 污染 overlay 组件

## 表格

- 列宽用 `min-width` 替代 `width`，让表格自适应撑满
- 表头：`background #F1F5F9, color #475569, fontWeight 600, fontSize 13px`

## 响应式

- 页面加 `resp-table` 类 → 表格超宽横向滚动
- 页面加 `resp-form` 类 → 小屏表单项竖向堆叠
- 移动端汉堡菜单已内置

## 测试

- Vitest（交互 watch）/ `pnpm test:run`（CI 单次）/ `pnpm test:coverage`（覆盖率）/ `pnpm test:ui`
- 全局 setup 覆盖：Element Plus 全部组件 + 图标 + Pinia 实例 + 浏览器 API mock + ElMessage mock

## SLS 日志

- `src/utils/sls.ts` 封装（单例 Tracker + STS 凭证刷新），`generateRequestId()` 生成 UUID v4
- `request.ts` 请求拦截器自动注入 `requestId` header
- 响应拦截器错误时自动调用 `sendSlsLog()`
- 上报字段：`system`(tob-WEB) / `url` / `statusCode` / `errorResponseBody` / `requestTime` / `userId` / `version` / `deviceType` / `terminalType`
- 本地 `VITE_SLS_ENABLED=false`

## 环境

`.env.development` → `.env.test` → `.env.production`，变量以 `VITE_` 开头，修改后重启 dev server

## 构建与部署

| 环境 | 命令 | 输出 |
|------|------|------|
| 开发 | `pnpm dev` | - |
| 测试 | `pnpm qa` | `dist-qa/` |
| 生产 | `pnpm pro` | `dist-pro/` |

- 构建后 `pnpm preview` 本地验证
- Nginx：`try_files $uri $uri/ /index.html`，`/assets/` 强缓存 1y

## 富文本编辑器 Tiptap

- 官方中文文档：https://tiptap.zhcndoc.com
- 已安装：`@tiptap/vue-3` `@tiptap/starter-kit` `@tiptap/extension-placeholder` `@tiptap/extension-image` `@tiptap/extension-link`
- 禁止使用其他富文本方案（如 Quill、CKEditor、wangEditor）

### 基本用法
```vue
<template>
  <editor-content :editor="editor" />
</template>

<script setup lang="ts">
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'

const editor = useEditor({
  extensions: [StarterKit, Placeholder.configure({ placeholder: '请输入内容...' })],
  content: '',
  editorProps: { attributes: { class: 'prose max-w-none focus:outline-none' } },
})
</script>
```

- 组件卸载时编辑器自动销毁，无需手动 dispose
- 需要图片/链接扩展时从 `@tiptap/extension-image` / `@tiptap/extension-link` 导入
- **禁止**安装 `@tiptap-pro/` 前缀的付费扩展包

## OSS 文件上传

- 已封装 `src/utils/oss.ts` — 基于 `ali-oss` SDK + STS 临时凭证直传
- 环境变量（预埋，后期配置后生效）：`VITE_OSS_REGION` / `VITE_OSS_BUCKET` / `VITE_OSS_DIR_IMAGE` / `VITE_OSS_DIR_FILE` / `VITE_OSS_STS_ENDPOINT`

### 用法
```ts
import { ossUpload, ossUploadImage, ossUploadMultiple } from '@/utils/oss'

// 上传单个文件
const { url, key } = await ossUpload(file, {
  onProgress: (p) => console.log(`${p}%`),
})

// 上传图片
const { url } = await ossUploadImage(imageFile, { dir: 'avatar' })

// 批量上传
const results = await ossUploadMultiple(files, {
  onTotalProgress: (p) => console.log(`总进度 ${p}%`),
})
```

- 支持自动重试（默认 3 次）、分片上传（1MB/片）、STS 凭证自动刷新
- STS 接口待后端实现（`GET /api/common/oss/sts-token`）（如 `@tiptap-pro/extension-*`）
# Design System Master File

> **LOGIC:** When building a specific page, first check `design-system/pages/[page-name].md`.
> If that file exists, its rules **override** this Master file.
> If not, strictly follow the rules below.

---

**Project:** TOB
**Generated:** 2026-07-09 18:27:31
**Category:** Analytics Dashboard

---

## Global Rules

### Color Palette

| Role | Hex | CSS Variable |
|------|-----|--------------|
| Primary | `#1E40AF` | `--color-primary` |
| Secondary | `#3B82F6` | `--color-secondary` |
| CTA/Accent | `#F59E0B` | `--color-cta` |
| Background | `#F8FAFC` | `--color-background` |
| Text | `#1E3A8A` | `--color-text` |

**Color Notes:** Blue data + amber highlights

### Typography

- **Heading Font:** Fira Code
- **Body Font:** Fira Sans
- **Mood:** dashboard, data, analytics, code, technical, precise
- **Google Fonts:** [Fira Code + Fira Sans](https://fonts.google.com/share?selection.family=Fira+Code:wght@400;500;600;700|Fira+Sans:wght@300;400;500;600;700)

**CSS Import:**
```css
@import url('https://fonts.googleapis.com/css2?family=Fira+Code:wght@400;500;600;700&family=Fira+Sans:wght@300;400;500;600;700&display=swap');
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
  background: #F59E0B;
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
  color: #1E40AF;
  border: 2px solid #1E40AF;
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
  background: #F8FAFC;
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
  border-color: #1E40AF;
  outline: none;
  box-shadow: 0 0 0 3px #1E40AF20;
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

**Style:** Data-Dense Dashboard

**Keywords:** Multiple charts/widgets, data tables, KPI cards, minimal padding, grid layout, space-efficient, maximum data visibility

**Best For:** Business intelligence dashboards, financial analytics, enterprise reporting, operational dashboards, data warehousing

**Key Effects:** Hover tooltips, chart zoom on click, row highlighting on hover, smooth filter animations, data loading spinners

### Page Pattern

**Pattern Name:** Pricing-Focused Landing

- **Conversion Strategy:** Annual discount 20-30%. Recommend mid-tier (most popular badge). Address objections in FAQ.
- **CTA Placement:** Each pricing card + Sticky CTA in nav + Bottom
- **Section Order:** 1. Hero (value proposition), 2. Pricing cards (3 tiers), 3. Feature comparison, 4. FAQ, 5. Final CTA

---

## Anti-Patterns (Do NOT Use)

- ❌ Ornate design
- ❌ No filtering

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
