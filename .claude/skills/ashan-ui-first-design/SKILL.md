---
name: ashan-ui-first-design
description: Use when starting any full-stack project — generates DESIGN.md (design tokens + page layouts + component specs) and HTML prototypes BEFORE any code is written. Triggers on phrases like "UI先行", "先出设计稿", "先做UI", "设计先行", "出原型图".
---

# 阿珊-UI先行设计

## Overview

在写任何一行前后端代码之前，先用 HTML 原型把页面画出来。原型即规格——前端照着做组件，后端照着定接口。一次审批，全链路对齐。

## 核心理念

```
传统流程：
  需求 → 写后端 → 写前端 → 发现不对 → 返工

UI先行流程：
  需求 → 出原型（HTML）→ 审批一次 → 前后端并行开发 → 完
```

**为什么省审批？** 原型出来后，页面长什么样、需要什么数据、有哪些交互——全部可视化了。前后端拿到的是同一份"施工图纸"，不需要中间反复确认。

---

## 第 1 步：生成 DESIGN.md（设计令牌）

**做什么：** 根据行业和平台，定义项目的视觉语言。

**内容：**
```markdown
# DESIGN.md — {项目名}

## 品牌调性
- 一句话风格定位
- 情绪关键词（3-5 个）
- 参考设计系统（Linear / Stripe / Airbnb / Apple...）

## 色彩系统
- Primary（主色）
- Secondary（辅色）
- Background（背景色）
- Text（文字色）
- Success / Warning / Error

## 字体
- 标题字体 + 正文字体
- 字号层级（h1-h4, body, caption）

## 圆角 & 阴影
## 间距系统（4px 基准）
## 动效风格（微交互 / 平滑 / 硬朗）
```

**审批：** 看一眼颜色和风格对不对，不对就改。

---

## 第 2 步：画页面原型（HTML）

**做什么：** 生成每个页面的静态 HTML 原型。

**工具：** 直接写 HTML + 内联 CSS，不需要框架。能在浏览器里打开看。

**每个页面原型包含：**
- 页面布局（导航 + 主体 + 底部）
- 所有 UI 组件（按钮、卡片、表单、列表、弹窗）
- 数据占位符（用真实模拟数据，不写"XXX"）
- 交互状态（hover、loading、empty、error）

**原型文件结构：**
```
prototypes/
├── index.html          ← 页面导航（链接到所有原型）
├── page-login.html     ← 登录页
├── page-dashboard.html ← 仪表盘
├── page-list.html      ← 列表页
├── page-detail.html    ← 详情页
├── page-form.html      ← 表单页
└── page-empty-error.html ← 空状态/错误状态
```

**审批：** 浏览器打开 index.html，每个页面点进去看一遍。确认后原型就是施工图纸，不再改。

---

## 第 3 步：从原型提取 API 契约

**做什么：** 根据每个页面需要的数据，反推后端需要提供什么接口。

**产出：** `API_CONTRACT.md`

```markdown
# API 契约

## 页面：登录页
→ POST /api/auth/login {email, password} → {access_token, refresh_token}
→ POST /api/auth/register {email, password} → {access_token, refresh_token}

## 页面：仪表盘
→ GET /api/user/me → {id, email, credits, tier}
→ GET /api/items?skip=0&limit=20 → {items: [...], total: N}

## 页面：列表页
→ ...
```

**产出物即规格，前后端各自开发时以此为界。**

---

## 完整产出物清单

```
项目目录/
├── DESIGN.md              ← 设计令牌（颜色/字体/间距）
├── API_CONTRACT.md        ← 前后端接口契约
└── prototypes/
    ├── index.html         ← 原型导航
    ├── page-login.html
    ├── page-dashboard.html
    ├── page-list.html
    ├── page-detail.html
    └── page-empty-error.html
```

---

## 审批后怎么用

```
DESIGN.md + 原型 审批通过
         │
    ┌────┴────┐
    ▼         ▼
  前端       后端
  照着原型   照着 API_CONTRACT.md
  写组件     写接口
    │         │
    └────┬────┘
         ▼
      联调验证
```

**你只需要审一次（原型阶段），后面都不用盯。**

---

## 参考工具整合

本技能支持结合以下外部工具增强原型效果：

| 工具 | 用途 | 可选/必选 |
|------|------|----------|
| Open Design (nexu-io) | 生成更丰富的设计系统 + 原型 | 可选 |
| 纯 HTML/CSS | 快速出原型（零依赖） | 必选 |
| DESIGN.md 规范 | 设计令牌标准化 | 必选 |

如果用户安装了 Open Design CLI，可以集成使用：
```bash
# 用 Open Design 生成设计系统
open-design init --skill web-prototype --design-system <name>
```

---

## 快速参考

| 用户说 | 做什么 |
|--------|--------|
| "先出设计稿，我要看页面长什么样" | 走第 1-2 步：出 DESIGN.md + HTML 原型 |
| "原型 OK 了，提取 API 契约" | 走第 3 步：出 API_CONTRACT.md |
| "UI 先行，出一套完整的施工图纸" | 走完 3 步，产出 DESIGN.md + 原型 + API_CONTRACT |
