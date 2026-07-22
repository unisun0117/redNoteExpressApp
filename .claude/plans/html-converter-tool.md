# HTML → MD / PPT 转换工具

## 需求分析

工作区有 **180+ 个 HTML 文件**，主要分布在：
- `html/` — 手绘风知识指南、PRD 文档、仪表盘（30+ 个）
- `kami/` — 模板、图表、演示（100+ 个）
- 其他散落的 HTML

这些 HTML 的共同特征：
- **单页应用结构**：用 `<section>` 划分逻辑"页面"（适合转 PPT 的 slide）
- **内容层次清晰**：h1/h2/h3 + p + ul/li + 卡片 div
- **大量 CSS/JS 装饰**：动画、渐变、手绘风格（需要剥离）
- **中文内容为主**

用户场景：
1. 把 HTML 知识指南 → **Markdown**，方便在 Notion/Obsidian/飞书里编辑和搜索
2. 把 HTML 演示/指南 → **PowerPoint**，直接用于汇报和分享

---

## 设计方案

### 技术选型：Python CLI 工具

选择 Python 的理由：
- 项目已有 Python 技术栈（FastAPI 后端）
- `beautifulsoup4` 解析 HTML 最成熟
- `python-pptx` 生成 PPTX 功能完整
- 可通过 `pip` 一键安装依赖

### 核心库

| 库 | 用途 |
|---|---|
| `beautifulsoup4` | HTML 解析，提取结构化内容 |
| `python-pptx` | 生成 .pptx 文件 |
| `markdown` | 生成干净的 .md 文件（可选，也可以手写） |

### 架构

```
html/
└── tools/                    ← 新建工具目录
    ├── requirements.txt      ← 依赖
    ├── convert.py            ← CLI 入口（主命令）
    ├── html_to_md.py         ← HTML → Markdown 转换器
    ├── html_to_pptx.py       ← HTML → PPTX 转换器
    └── README.md             ← 使用说明
```

### HTML → Markdown 转换策略

```
HTML 元素          →  Markdown
──────────────────────────────
<h1>              →  # 标题
<h2>              →  ## 标题
<h3>              →  ### 标题
<p>               →  段落文字
<ul><li>          →  - 列表项
<ol><li>          →  1. 列表项
<strong>          →  **加粗**
<em>              →  *斜体*
<table>           →  Markdown 表格
<a href="...">    →  [文字](url)
<img>             →  ![alt](src)
<code>/<pre>      →  代码块
<div class="card">→  提取文字 + 用 blockquote 包裹
<style>/<script>  →  删除
```

**核心挑战**：HTML 中大量 CSS 装饰 div（card、sticky、grid）需要提取文字内容而非结构。

### HTML → PPTX 转换策略

```
每个 <section>    →  一页 Slide
<section> 内的 h2 →  Slide 标题
<section> 内的 h3 →  内容小标题（加粗）
<section> 内的 p  →  正文内容
<section> 内的 li →  项目符号列表
<section> 内的 .card / .sticky → 文本框
```

**布局策略**：
- **标题 + 正文**：最常见的模式，标题在上，正文在下方
- **卡片网格**：多个 `.card` 并排 → PPT 里用多个文本框分列
- **宽图/代码**：尽量保留原文格式
- **最大字数控制**：每页 slide 不超过 ~200 字，超出自动拆分

### CLI 用法设计

```bash
# 单文件转换
python tools/convert.py input.html --to md
python tools/convert.py input.html --to pptx
python tools/convert.py input.html --to both    # 同时生成 md 和 pptx

# 批量转换整个目录
python tools/convert.py html/Agent/ --to both --output output/

# 指定输出路径
python tools/convert.py input.html --to md -o result.md
python tools/convert.py input.html --to pptx -o slides.pptx
```

---

## 实施任务

### Task 1: 搭建工具骨架
- 创建 `html/tools/` 目录
- 写 `requirements.txt`（beautifulsoup4, python-pptx, markdown）
- 写 `convert.py` CLI 入口（argparse，支持单文件和目录）

### Task 2: 实现 HTML → Markdown 转换器
- 用 BeautifulSoup 解析 HTML
- 实现元素到 Markdown 的映射
- 处理特殊情况：嵌套 card、sticky note、代码块
- 剥离 `<style>` 和 `<script>`

### Task 3: 实现 HTML → PPTX 转换器
- 用 BeautifulSoup 解析 HTML
- 每个 `<section>` 映射为一页 slide
- 实现布局策略（标题页、内容页、列表页）
- 添加基本样式（字体大小、颜色、间距）

### Task 4: 写 README + 验证
- 写使用说明文档
- 用几个真实 HTML 文件测试
- 确保中文不乱码

---

## 不做的事情（范围边界）

- ❌ 不保留 CSS 动画/特效（MD 和 PPT 都不支持）
- ❌ 不转换交互式 JS 逻辑
- ❌ 不做反向转换（MD→HTML、PPT→HTML）
- ❌ 不做 Web UI（先用 CLI，后续可以加）
- ❌ 不处理 `<canvas>` / SVG 图表（太难，先跳过）
