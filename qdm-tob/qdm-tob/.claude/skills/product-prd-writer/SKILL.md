---
name: product-prd-writer
description: 三端（admin/mobile/h5）PRD原型HTML生成与菜单注册 — 从SPEC文档生成可运行的原型界面，注册到对应终端的导航框架
version: 2.0.0
author: Product Engineering Team
triggers:
  - 生成PRD
  - 产品需求HTML
  - PRD原型
  - 原型PRD
  - 原型HTML
  - 生成企业微信H5
  - 生成小程序H5
  - 功能页面
  - 生成原型
  - 后台页面
  - 小程序页面
  - 企微页面
categories:
  - product-management
  - documentation
  - frontend
dependencies: []
tools: []
---

# Skill: Product PRD Writer — 多端原型 HTML 生成与注册

## 1. 目标

从 `product/spec/<NN>-<模块名>/<功能名>.md` 读取 SPEC 规范，生成可运行的原型 HTML，并注册到对应终端的导航框架中。

支撑 QDM-TOB 三端：
- **PC 后台**（运营/管理员）→ `admin.html` 框架（侧边栏 + iframe）
- **微信小程序**（客户）→ `mobile.html` 框架（含内置 Tabbar、宿主重置监听器 + iframe）
- **企业微信 H5**（业务员）→ `h5.html` 框架（含内置 Tabbar、宿主重置监听器 + iframe）

⚠️ 核心原则：
- 所有终端的`手机壳`、`浏览器外壳`、`全局导航（NavBar、Tabbar）`均已由底座框架实现。AI 的核心任务是降维交付——只生成嵌入在 iframe 内部的纯粹业务功能内页，严禁自带任何外壳容器、外层边框与全局导航组件。

---

## 2. 目录结构

```
product/
├── spec/                                    ← SPEC + 原型 HTML（工作区）
│   ├── README.md                            ← 产品结构总览，菜单定义
│   ├── 01-商品管理/
│   │   ├── 商品资料.md                       ← SPEC Markdown 规范文档
│   │   ├── 商品资料-原型.html                 ← PRD 原型 HTML（与 .md 同目录）
│   │   ├── 商品价格.md
│   │   └── 商品价格-原型.html
│   ├── 02-客户管理/
│   │   ├── 客户档案.md
│   │   ├── 客户档案-原型.html
│   │   ├── 小程序登录注册模块.md
│   │   └── 小程序登录注册模块-原型.html
│   └── NN-<模块名>/                          ← 按业务域编号 + 中文名
│       ├── <功能名>.md                       ← SPEC 文档
│       └── <功能名>-原型.html                 ← PRD 原型 HTML
│
└── prd/                                     ← 导航框架层（三个壳子）
    ├── admin.html                            ← PC 后台框架（侧边栏 + iframe）
    ├── menu.js                               ← PC 后台菜单数据
    ├── mobile.html                           ← 小程序框架（TabBar + iframe）
    └── h5.html                               ← 企微 H5 框架（TabBar + iframe）
```

### 2.1 核心原则

1. **原型 HTML 放在 spec 模块目录下**，与 `*.md` 同目录，命名：`<功能名>-原型.html`
2. **三个框架壳子放在 `product/prd/` 下**
3. **`menu.js` 中的 `url`** 用相对路径从 `prd/` 指向 `../spec/<NN>-<模块名>/<功能名>-原型.html`

---

## 3. 三端框架规范

### 3.1 PC 后台（admin.html）

- **终端**：PC 浏览器
- **用户角色**：运营/管理员
- **导航模式**：左侧可折叠菜单 + 右侧 iframe 内容区
- **AI 交付边界**：只生成右侧内容区内部的表单、表格与数据视图。
- **菜单来源**：`product/spec/README.md` → "后台管理菜单目录" 章节
- **框架文件**：`product/prd/admin.html` + `product/prd/menu.js`

```javascript
// prd/menu.js 示例
{
    id: "system",
    title: "系统管理",
    icon: "Tools",
    children: [
        {
            id: "/system/account",
            name: "账户管理",
            url: "../spec/06-基础管理/用户管理-原型.html"
        },
    ],
},
```

### 3.2 微信小程序/企微 H5 内页 (mobile.html/h5.html)

- **终端**：微信小程序 WebView 模拟
- **mobile 用户角色**：客户（下单人）
- **h5 用户角色**：业务员
- **宿主模式**：固定宽度 375px 的 iphone-shell 外壳。自带全局 van-nav-bar（通过 loadCount 脉搏监听器自动识别内部跳转并显示返回键）与底部 van-tabbar
- **AI 交付边界**：绝对去壳化。严禁编写 .iphone-shell、.mobile-shell 等外壳容器；严禁在根视图自带全局 van-nav-bar 和 van-tabbar
- **内页跳转黑科技联动**：若涉及“点击列表卡片进入详情页”的交互，AI 须在内页组件上绑定点击事件，通过 window.location.href = 'xxx-原型.html' 直接在当前 iframe 内做页面替换。此举会触发父框架的 loadCount++，从而令父框架自动浮现“返回”按钮；点击父框架返回时，父框架会通过 key++ 自动销毁并重置根视图。
- **mobile/h5主题样式**：`#FF0000`/`#3B82F6`, 这2个主题不能一起使用
- **引入样式**：`<script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>`

---

## 4. PRD 原型 HTML 模板规范

### 4.1 PC 后台内页标准模板

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>页面标题</title>
    <link rel="stylesheet" href="https://unpkg.com/element-plus/dist/index.css">
    <style>
        :root { --primary: #409eff; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;
            background: #f0f2f5;
            margin: 0;
            padding: 10px;
        }
    </style>
</head>
<body>
    <div id="prd-root">
        <!-- 原型内容 -->
    </div>

    <!-- 需要交互时引入，纯静态可省略 -->
    <script src="https://unpkg.com/vue@3/dist/vue.global.js"></script>
    <script src="https://unpkg.com/element-plus"></script>
    <script src="https://unpkg.com/element-plus/dist/locale/zh-cn.js"></script>
    <script>
        const { createApp } = Vue;
        const app = createApp({ /* data/methods */ });
        app.use(ElementPlus, { locale: ElementPlusLocaleZhCn }).mount('#prd-root');
    </script>
</body>
</html>
```

### 4.2 移动端（小程序/H5）内页标准模板

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover">
    <title>业务内页</title>
    <link rel="stylesheet" href="https://unpkg.com/vant@4/lib/index.css">
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <style>
        :root {
            /* 小程序与h5主题色不能同时使用 */
           /* --van-primary-color: #ff0000; *//* 小程序 */
           /* --van-primary-color: #3B82F6;  *//* H5 */
        }
        body {
            background-color: #f8fafc;
            font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif;
            margin: 0;
            padding: 12px; /* 统一留边，规避视窗粘连 */
            box-sizing: border-box;
        }
        ::-webkit-scrollbar { display: none; }
    </style>
</head>
<body>
    <div id="prd-root" class="space-y-3 pb-16">
        </div>

    <script src="https://unpkg.com/vue@3/dist/vue.global.js"></script>
    <script src="https://unpkg.com/vant@4/lib/vant.min.js"></script>
    <script>
        const { createApp, ref } = Vue;
        createApp({
            setup() {
                return {};
            }
        }).use(vant).mount('#prd-root');
    </script>
</body>
</html>
```

### 4.3 各端适配要点

| 维度 | 后台 (admin) | 小程序 (mobile) | 企微 H5 (h5) |
|------|-------------|----------------|--------------|
| **屏宽** | 桌面宽度（≥1200px） | 375px | 375px |
| **UI 组件库** | Element Plus | Vant 4 | Vant 4 |
| **字号基准** | 14px 正文 | 14px 正文 | 14px 正文 |
| **表单密度** | 宽松间距 | 紧凑间距 | 紧凑间距 |
| **导航方式** | 侧边栏菜单 | 底部 TabBar | 底部 TabBar |

### 4.4关键开发约束（强制执行）

1. **Tailwind 引入流分流：**
- **PC 后台管理内页**：**禁止**引入 Tailwind CDN（防止与 Element Plus 样式级联冲突）。
- **移动端（小程序/H5）内页**：**必须**引入 `<script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>`，全面使用 `Tailwind v4 `原子类进行高保真轻量化排版

2. **禁止自带外部壳与导航条**：严禁包含任何模拟手机边框的容器样式。严禁在根节点创建` van-nav-bar`（带有返回键的头部）与 `van-tabbar`（底部页签）

3. **标签必须完美闭合（Vue 3 标准）**：所有非原生 void 标签（如 el-* 组件及 van-* 组件）绝对禁止自闭合。例如：`<van-cell />` 必须严格编写为` <van-cell></van-cell>`，`<el-input /> `必须编写为`<el-input></el-input>`;任何遗漏直接判定为语法错误
4. **Vue 固定挂载点**：所有端统一使用 `#prd-root` 作为挂载目标，严禁使用 `#app`（已被宿主底座占用）
5. **禁止添加页面标题/副标题**——不要 `<h1>` 或 `.prd-title` / `.prd-subtitle`，页面名称由父框架菜单体现
6. **所有非 void 标签必须完整闭合**——原生 HTML 浏览器不支持自闭合写法，`<el-input />` / `<el-button />` / `<el-table-column />` / `<el-select />` / `<el-date-picker />` / `<el-option />` 等一律写成 `<el-input></el-input>`、`<el-button></el-button>`、`<el-table-column></el-table-column>`。只有原生 void 元素（`<br>`、`<hr>`、`<img>`、`<input>`）可自闭合。**任何遗漏视为生成错误**
7. *Vue 挂载点必须用 `#prd-root`**——不能用 `#app` 或 `body`（三端框架已占用）**
8. 样式写在内联 `<style>` 中**——不引入外部 CSS 文件**
9. 纯静态原型无需 **Vue/ElementPlus CDN**, **路径全部用相对路径**

### 4.5 页面布局规范（强制）

4.5.1 后台管理列表页面**必须**严格按两段式布局：

```
┌─ 卡片1: 查询区（background:#fff, border-radius:8px, padding:10px; padding-bottom:0px）─┐
│   el-form inline 布局                                               │
│   输入框 / 下拉框 / 日期选择 + [查询] [重置]                          │
└─────────────────────────────────────────────────────────────────────┘
                            ↕ margin-bottom: 10px
┌─ 卡片2: 操作&数据区（background:#fff, border-radius:8px, padding:10px）─┐
│   操作按钮：[新增] [批量删除] [导出] 等                                  │
│   表格：el-table（多选列 + 状态标签 + 操作列）                           │
│   分页：el-pagination（底部左对齐）                                     │
└───────────────────────────────────────────────────────────────────────┘
```

4.5.2 移动端根页面（指标 + 菜单 + 卡片流布局）

- 数据指标层：采用 grid grid-cols-2 gap-3 构筑，背景色使用现代高饱和度渐变 bg-gradient-to-br from-... to-...，大圆角 rounded-2xl，文字反白。
- 高频金刚区：纯白底 bg-white，微阴影 shadow-xs，大圆角 rounded-2xl。内嵌 van-grid :column-num="4" :border="false" icon-size="26px"。
- 简短数据卡片流：块状 div 独立平铺，背景纯白，圆角 rounded-xl。卡片左侧边框利用 border-l-4 附加状态色条（如 border-amber-400 提示待审核）。操作按钮统一置于卡片右下角，通过 flex justify-end gap-2 编排，尺寸锁死为 size="small" round。

4.5.3 移动端子级页面（表单/详情/审批页布局）

- 只读数据平铺：利用 van-cell-group inset 块状包裹 van-cell 组件，进行分组分格的信息平铺展示。
- 状态标签位置：重要状态标签（如 van-tag）统一紧跟在核心标题后，或在单元格右侧 value 位置高亮呈现。
- 底部悬浮吸底操作栏：如涉及审批或提交动作，按钮区域必须使用 Tailwind 的定位类 fixed bottom-0 left-0 right-0 p-3 bg-white border-t border-slate-100 flex gap-2 z-50 牢牢锁定在视窗最底部，同时给外层容器 #prd-root 注入 pb-16 或 pb-20 的下留白，防止内容被操作栏遮挡。

**强制要求**：
- 查询区和操作&数据区各自独立 `div`，`background: #fff`，`border-radius: 8px`，`padding: 10px`，区间 `margin-bottom: 10px`
- 查询区独立 `div`，`background: #fff`，`border-radius: 8px`，`padding: 10px`，`padding-bottom:0px`，区间 `margin-bottom: 10px`
- 操作按钮和表格放在**同一个 div** 内（操作&数据区），并且按钮 **必选**左对齐
- 表格**必须**带多选列 `type="selection"`
- 表格**必须**带分页 `el-pagination`，`justify-content: flex-start;`
- 表格**必须**添加表头样式 `:header-cell-style="{background:'#f5f7fa', color:'#606266', fontWeight:500}"`

小程序和企微 H5 不使用宽表格，改用**搜索栏 + 卡片/列表**布局：
- Mobile：`van-search` + 卡片列表（Vant 4）
- H5：`van-search` + `van-cell` 列表（Vant 4）

---

## 5. 注册流程

生成原型 HTML 后，同步更新对应终端的 `menu-*.js`：

| 步骤 | 操作 |
|------|------|
| 1 | 确认原型属于哪个**终端**（后台/小程序/企微） |
| 2 | 编辑 `prd/menu-*.js`，找到对应菜单项 |
| 3 | 添加 `url` 字段，指向 `../spec/<NN>-<模块名>/<功能名>-原型.html` |
| 4 | 无 url 的菜单项显示占位页（框架已内置） |

---

## 6. 质量门禁

| # | 检查项 | 验证方式 |
|---|--------|---------|
| 1 | 原型文件在 spec 模块目录下 | `ls product/spec/<NN>-<模块名>/<功能名>-原型.html` |
| 2 | menu-*.js 已更新 url | `grep '原型.html' product/prd/menu-*.js` |
| 3 | HTML 未引入 Tailwind CDN | `grep -i tailwind product/spec/.../<功能名>-原型.html`（应为空） |
| 4 | 无非 void 标签自闭合 | `grep '/>' product/spec/.../<功能名>-原型.html`（el-* 标签应无匹配） |
| 5 | 无页面标题/副标题 | `grep 'prd-title\|prd-subtitle\|<h1>' product/spec/.../<功能名>-原型.html`（应为空） |
| 6 | Vue 挂载点是 `#prd-root` | `grep '\.mount' product/spec/.../<功能名>-原型.html` |
| 7 | Element Plus 已配置中文语言包 | `grep 'ElementPlusLocaleZhCn' product/spec/.../<功能名>-原型.html` |
| 8 | 后台页面严格两段式（查询区 → 操作&数据区） | 对照 4.4 布局规范检查 |
| 9 | 后台表格含多选列 + 分页 + 表头样式 | 检查 `type="selection"`、`el-pagination`、`:header-cell-style` |
| 10 | 浏览器打开对应框架，点菜单可正常 iframe 加载 | 预览验证 |

---

## 7. AI 执行清单

1. **读取 SPEC**：`product/spec/<NN>-<模块名>/<功能名>.md`
2. **判断终端**：根据 SPEC 的"子模块 | 终端"列判断是后台/小程序/企微
3. **生成原型 HTML**：后台严格按 4.4 两段式；mobile/H5 用搜索+卡片列表；遵守 4.3 全部 8 条关键约束
4. **注册菜单**：编辑 `prd/menu-*.js`，加 `url`
5. **提醒验证**：打开对应框架 HTML 验证
