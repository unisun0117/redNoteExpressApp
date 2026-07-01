
---
name: ashan-ui-page-builder
description: Use when the user asks to build UI pages, dashboards, admin panels, or any web interface. Triggers on phrases like "设计一个...界面", "做一个...页面", "开发...后台", "搭建...仪表盘", "画一个...页面", "帮我设计...UI", "build a...dashboard", "create a...page", "design a...UI". Covers layout patterns, component recipes, color systems, and China-network-safe CDN strategies.
---

# UI Page Builder — 页面搭建技能

## Overview

当用户要求开发任何 Web UI 页面时，触发此技能。核心原则：**零/低依赖、单文件可运行、每个页面有独特的布局个性**。

---

## 第 0 步：分析需求，匹配布局模式

拿到需求后，先判断页面类型，再从布局模式库中选择最合适的布局。**禁止所有页面用同一种网格排列——每个页面必须有独特的视觉个性。**

### 布局模式库

| 模式 | 适用场景 | 结构特征 |
|------|----------|----------|
| **Hero Banner + 非对称网格** | 工作台/Dashboard 首页 | 顶部渐变横幅 + 快捷入口 + 2/1/1 错落网格 + 底部三等分 |
| **Bento Grid（12列不等宽）** | 数据密集的管理页 | `span 5 + span 7` 交错，卡片尺寸随内容变化 |
| **左右分栏 + 时间轴** | 日历/排班/日程 | 左 70% 日历区 + 右 320px 时间轴或侧栏 |
| **顶部筛选 + 全宽表** | 列表/档案/病历管理 | 统一筛选栏 + 全宽表格 + 底部分页 |
| **KPI Hero + 多列图表** | 报表/统计 | 多色渐变数字卡片 + 2-3 列图表 + 汇总表 |
| **三栏（侧栏-中央-侧栏）** | 趋势分析/数据洞察 | 240px 左指标面板 + 中央大图区 + 240px 右对比面板 |
| **两列表单卡片** | 设置/配置页 | 左右各一列，左个人信息+右安全设置，下方系统配置卡片 |
| **欢迎横幅 + 四宫格** | Landing/引导页 | 大图 Banner + 功能入口网格 |

### 选择原则

- 同一个项目里，**每个页面用不同的布局模式**
- 列表/表格类页面可以复用"筛选+全宽表"模式（但颜色和细节要有差异）
- Dashboard 用 Hero Banner 或非对称网格
- 数据可视化用三栏或 KPI Hero
- 表单/设置用两列卡片

---

## 第 1 步：确定技术方案

### 优先级（从高到低）

1. **纯 HTML + Tailwind CDN + 原生 JS**（首选）
   - 单文件零依赖，浏览器直接打开
   - Tailwind CDN：`https://cdn.tailwindcss.com`（Cloudflare，国内通常可通）
   - 无需 Node.js、无需构建、无需 npm install
   
2. **纯 HTML + 全内联 CSS**（备选，Tailwind 被墙时用）
   - 所有样式写在 `<style>` 内，彻底零外部依赖
   
3. **React/Vue CDN**（不推荐国内环境）
   - `esm.sh` 在国内不稳定，禁止使用
   - 如需 React，用 `cdn.jsdelivr.net` 的 UMD 版本（国内有节点）
   - 绝对不要用 `esm.sh` 的 UMD 路径（它只服务 ESM 格式）

### CDN 白名单

| 资源 | 可用 CDN | 国内可用性 |
|------|----------|-----------|
| Tailwind CSS | `cdn.tailwindcss.com` | ⚠️ Cloudflare，通常可通 |
| React UMD | `cdn.jsdelivr.net/npm/react@18/umd/react.production.min.js` | ✅ 国内有节点 |
| Babel Standalone | `cdn.jsdelivr.net/npm/@babel/standalone/babel.min.js` | ✅ 国内有节点 |
| Google Fonts | `fonts.googleapis.com` | ❌ 大概率不通 |

### 字体回退策略

```css
font-family: 'Inter', 'Noto Sans SC', system-ui, sans-serif;
```
Google Fonts 不通时会自动回退到系统字体，页面不会崩。

---

## 第 2 步：定义设计令牌（Design Tokens）

根据行业和风格确定色彩、字体、圆角、阴影。

### 行业配色

| 行业 | 主色 | 辅色 | 强调色 |
|------|------|------|--------|
| 医疗健康 | `#3366ff` 蓝 | `#10b981` 绿 | `#f59e0b` 琥珀 |
| 金融科技 | `#1e40af` 深蓝 | `#059669` 绿 | `#dc2626` 红 |
| 电商零售 | `#f97316` 橙 | `#2563eb` 蓝 | `#10b981` 绿 |
| SaaS/科技 | `#7c3aed` 紫 | `#3b82f6` 蓝 | `#06b6d4` 青 |
| 教育 | `#0891b2` 青 | `#f59e0b` 黄 | `#10b981` 绿 |
| 政务 | `#dc2626` 红 | `#1e40af` 深蓝 | `#ca8a04` 金 |

### CSS 变量模板

```css
:root {
  --bg:       #f1f5f9;   /* 页面底色 */
  --card:     #ffffff;   /* 卡片底色 */
  --border:   #e8ecf1;   /* 边框色 */
  --text:     #1e293b;   /* 主文字 */
  --muted:    #94a3b8;   /* 次要文字 */
  --primary:  #3366ff;   /* 主色 */
  --pdark:    #1a40e8;   /* 主色深 */
  --success:  #10b981;   /* 成功/正向 */
  --warn:     #f59e0b;   /* 警告 */
  --danger:   #f43f5e;   /* 危险/错误 */
  --violet:   #8b5cf6;   /* 紫色辅助 */
  --cyan:     #06b6d4;   /* 青色辅助 */
  --radius:   14px;       /* 圆角 */
  --shadow:   0 1px 3px rgba(0,0,0,.04);
  --shadow-lg: 0 10px 30px rgba(30,64,175,.08), 0 3px 8px rgba(0,0,0,.04);
}
```

---

## 第 3 步：搭建组件

### 3.1 指标卡片 (Metric Card)

```html
<div class="metric-card">
  <div class="metric-icon" style="background:#eef4ff;color:var(--primary);">👥</div>
  <div>
    <div class="metric-num">2,486</div>
    <div class="metric-label">总患者数</div>
    <span class="metric-badge" style="background:#ecfdf5;color:#059669;">↑ 3.2%</span>
  </div>
  <!-- 可选：迷你折线图 SVG -->
</div>
```

样式要点：
- `metric-num`: 26-32px, font-weight 800, letter-spacing -1px
- `metric-icon`: 42-46px 圆角方块，浅色背景 + 主色图标
- `metric-badge`: 圆角 pill，浅色背景 + 深色文字

### 3.2 热力图 (Heatmap)

用 CSS Grid 实现，颜色梯度：
- `≤4%`: 浅蓝 `#dbeafe`
- `4-8%`: 中蓝 `#93c5fd`  
- `8-12%`: 浅黄 `#fde68a`
- `12-16%`: 深黄 `#fbbf24`
- `>16%`: 红色 `#f87171`

悬停 `transform: scale(1.18)` + 阴影增强。

### 3.3 环形进度条 (Progress Ring)

用 SVG circle + `stroke-dasharray` / `stroke-dashoffset` 实现：

```javascript
const radius = (size - strokeWidth * 2) / 2;
const circumference = 2 * Math.PI * radius;
const offset = circumference - (percentage / 100) * circumference;
// <circle stroke-dasharray={circumference} stroke-dashoffset={offset} />
```

关键：`transform="rotate(-90 cx cy)"` 让进度从顶部开始。

颜色规则：
- `≥80%`: 绿色 `#10b981`
- `60-79%`: 黄色 `#f59e0b`
- `<60%`: 红色 `#f43f5e`

### 3.4 柱状图 (Bar Chart)

纯 CSS + 原生 JS 渲染，无需 Chart.js 等库：

```javascript
function barChart(containerId, values, labels, maxHeight) {
  const max = Math.max(...values);
  container.innerHTML = values.map((v, i) => `
    <div style="flex:1;display:flex;flex-direction:column;align-items:center;gap:4px;">
      <span style="font-size:10px;">${v}</span>
      <div style="width:100%;height:${v/max*maxHeight}px;background:${i===values.length-1?'var(--primary)':'#93c5fd'};border-radius:5px 5px 0 0;"></div>
      <span style="font-size:10px;color:var(--muted);">${labels[i]}</span>
    </div>
  `).join('');
}
```

### 3.5 实时队列 (Live Queue)

带脉冲动画的状态指示器：

```css
@keyframes pulse-amber {
  0%,100% { box-shadow: 0 0 0 0 rgba(245,158,11,.4); }
  50%    { box-shadow: 0 0 0 7px rgba(245,158,11,0); }
}
```

队列状态标签配色：
- 等候中：`#fffbeb` 背景 + `#f59e0b` 文字 + pulse-amber
- 进行中：`#eef4ff` 背景 + `#3366ff` 文字 + pulse-blue
- 已完成：`#ecfdf5` 背景 + `#10b981` 文字 + 无动画

### 3.6 数据表格 (Data Table)

```css
.tbl th {
  font-size: 9px; font-weight: 700; color: var(--muted);
  text-transform: uppercase; letter-spacing: 0.5px;
  padding: 7px 10px; border-bottom: 2px solid #e2e8f0;
}
.tbl td {
  padding: 10px; font-size: 12px; color: #334155;
  border-bottom: 1px solid #f8fafc;
}
.tbl tbody tr:hover td { background: #f8fafc; }
```

状态徽章：
- 正常：`#ecfdf5` bg + `#059669` 绿文字
- 警告：`#fffbeb` bg + `#d97706` 琥珀文字
- 异常：`#fef2f2` bg + `#dc2626` 红文字

---

## 第 4 步：注入模拟数据

**所有数据必须是真实感和业务语义的模拟数据，不能用 "XXX"、"测试"、"123"。**

- 中文姓名、真实科室名、合理年龄分布
- 日期用最近的日期（当前月前后）
- 数字要有合理范围（门诊量 50-400，年龄 0-100，百分比 0-100%）
- 趋势百分比要跟实际数字匹配

---

## 第 5 步：验证 & 纠错

生成代码后必须检查：

| 检查项 | 说明 |
|--------|------|
| **JS 语法** | 确保没有 Python 语法混入（如 `for x in y`、列表推导式等） |
| **CDN 可用** | 检查所有外部资源 URL 是否正确且国内可访问 |
| **DOM ID 唯一** | 所有 `getElementById` 的 ID 在 HTML 中确实存在 |
| **CSS 变量** | 在 JS 中通过 `setProperty` 设置的变量确实被 CSS 引用 |
| **模板字符串闭合** | 反引号模板字符串中的 `${}` 表达式语法正确 |
| **页面切换** | 多页面应用确保 `innerHTML` 替换后 DOM 事件和样式仍然生效 |

---

## 完整示例

参考文件：`E:\AI_Workspace\100_UI_Skills_Html\medical-dashboard.html`

该文件展示了此技能所有模式的实际应用：
- 7 个页面，7 种布局模式
- 单文件 700+ 行，零框架依赖
- 侧边栏导航 + 页面切换
- 所有组件模式（指标卡、热力图、环形进度、队列、表格、柱状图、时间轴、开关 Toggle）

---

## 快速参考

| 用户说 | 做什么 |
|--------|--------|
| "做一个医疗后台" | 医疗蓝 + Bento Grid + 患者管理组件 |
| "搭一个数据看板" | Hero Banner + 指标卡 + 多列图表 |
| "设计一个管理界面" | 分析行业 → 选配色 → 匹配布局 → 选组件 → 写代码 |
| "把全部页面都做出来" | 列出所有页面 → 每个页面分配不同布局模式 → 逐页实现 |
