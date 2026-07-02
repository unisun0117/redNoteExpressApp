---
name: ashan-knowledge-column
description: Use when the user wants to learn a new technical domain from zero (databases, backend, deployment, DevOps, auth, Linux), asks for a "专栏" or "从0到1" tutorial series, says "前端转全栈", or wants structured knowledge articles with frontend analogies. Triggers on phrases like "帮我从0到1学XX", "XX入门专栏", "写个XX专栏", "系统学XX".
---

# 阿珊知识专栏生成器

## Overview

把任意技术领域拆成一门 10 篇专栏，用前端已知概念做类比桥接。核心原则：**用你懂的东西，讲你不懂的东西**。每篇不只讲技术，还讲"前端为什么要关心这个"。

## 品牌人格

| 维度 | 设定 |
|------|------|
| **身份** | 在职前端，正在自学全栈，副业写专栏 |
| **语气** | 像同事给你讲，不端着不炫技 |
| **署名** | "—— 阿珊，前端开发者 & 全栈学习者" |

## 专栏结构（10 篇固定认知弧线）

```
概念 → 原理 → 环境 → 实操 → 深挖 → 实战 → 坑点 → 优化 → 面试 → 进阶

第 1 篇  概念认知       这是啥？为什么前端要学？
第 2 篇  核心原理       底层怎么跑？
第 3 篇  环境与工具     装什么？怎么配？Hello World
第 4 篇  基础操作       入门级实操
第 5 篇  核心概念深挖   最重要的 3 个概念彻底搞懂
第 6 篇  实战应用       在你的项目里怎么用
第 7 篇  常见坑点       前 10 个坑我都帮你踩过了
第 8 篇  性能与优化     别等出了问题再优化
第 9 篇  面试高频考点   面试会问什么 + 怎么答
第 10 篇 进阶路线图     下一步学什么 + 跟什么技术组合
```

### 每篇三件事必须做

1. **前端类比** — 用你已经懂的前端概念解释新概念
2. **可以跑的代码** — 不是伪代码，是真能复制粘贴跑起来的
3. **个人金句** — 每篇至少 1 句阿珊经验

## 核心武器：前端类比桥接表

### 数据库 ← → 前端

| 后端概念 | 前端类比 |
|---------|---------|
| 数据库表 | `Array<Object>` — 一个巨大的 JSON 数组 |
| SELECT 查询 | `array.filter()` |
| WHERE 条件 | `filter(item => item.type === "expense")` |
| INDEX 索引 | `Map` 的 O(1) 查找 — 有索引快 1000 倍 |
| JOIN 联表 | 两个数组按 id 匹配合并 |
| PRIMARY KEY | `data.find(item => item.id === "xxx")` |
| FOREIGN KEY | `{ userId: "abc" }` 指到另一个数组 |
| Transaction 事务 | `Promise.all([a, b, c])` — 要么全成功要么全失败 |
| Migration 迁移 | Git commit — 数据库结构的版本管理 |
| ORM | JSX → DOM 的映射 — 你写 Python，ORM 生成 SQL |
| 金额存分不存元 | 前端 Canvas 用 `devicePixelRatio` — 避免浮点精度 |

### 后端 ← → 前端

| 后端概念 | 前端类比 |
|---------|---------|
| HTTP Server | `addEventListener('click', handler)` — 监听端口等请求 |
| Router | React Router `<Route path="/api/auth">` |
| Middleware | axios interceptor — 请求响应过程中拦截 |
| Controller | 事件处理函数 `(req) => res` |
| JWT Token | 登录后的 cookie — 存在浏览器，每次请求带上 |
| Dependency Injection | React Context Provider |
| Pydantic Model | TypeScript interface — 定义数据结构 + 自动校验 |
| Async/Await | 前端的 async/await，一模一样 |
| 连接池 | fetch 的 keep-alive 连接复用 |
| try/except | try/catch — 后端异常处理就是这 |

### 部署/DevOps ← → 前端

| DevOps 概念 | 前端类比 |
|-----------|---------|
| CI/CD | `git push` → Vercel 自动 build |
| Docker | `node_modules` 的终极版 — 整个环境打包 |
| Nginx | Vite dev server 的 proxy |
| 环境变量 | `.env` 文件，不同环境不同值 |
| DNS | `/etc/hosts` — 域名到 IP 的映射 |
| SSL/HTTPS | `https://` — 没有 HTTPS 浏览器会标记不安全 |
| CDN | `import "cdn.xxx.com/react"` — 就近访问静态文件 |

## 单篇文章模板（🚨 强制 7 段式结构）

**每篇文章必须包含以下 7 个章节，缺一不可。字数不低于 1000 中文字符。**

```markdown
# {专栏名} 从0到1 第{N}篇：{标题}

@[TOC](目录)

## 摘要

> {100-150字。本文讲什么？适合谁看？学完能做什么？用引用格式突出。}

## 引言

{2-3段。从读者已知的场景自然引出话题。为什么要学这个？解决了什么实际问题？和你已有的前端知识有什么关联？}

## 基础知识储备

{3-5个前置知识点，让读者自我检查：}
- 知识点1
- 知识点2
- 知识点3

## 正文

{核心内容，2-3个子章节（### 小标题）。必须包含：}
{1. 前端类比 — 用 React/Vue/JS 的具体概念解释新知识}
{2. 可运行的代码 — 复制粘贴就能跑，不是伪代码}
{3. 实践建议 — 至少1条"什么时候用/什么时候不用"的指引}

### 子章节1

{内容 + 前端类比 + 代码示例}

### 子章节2

{内容 + 前端类比 + 代码示例}

## 总结

{3-5条核心要点，用编号列表：}
1. 要点1
2. 要点2
3. 要点3

## 注意事项

{2-4条实践中容易忽略的提醒：}
- 注意点1
- 注意点2

## 坑点

| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| ... | ... | ... | ... |
| ... | ... | ... | ... |
| ... | ... | ... | ... |
```

### 📏 字数强制要求

**每篇文章正文（不含代码块）必须 ≥ 1000 个中文字符。** 如果不满，扩充"前端类比"和"实践建议"部分。

### ❌ 禁止出现的元素

- 不要出现"阿珊"、"阿珊说"、"阿珊小贴士"（用户要求去掉）
- 不要用 emoji 标题前缀（如 `## 📌`），保持专业简洁
- 不要出现"—— 阿珊，前端开发者 & 全栈学习者"署名
- 不要在正文里使用一级标题 `#`（保留给文章主标题）

## 生成流程

### Step 1：确认领域粒度

如果领域太大，拆成子专栏：
- ❌ "后端从0到1" — 太大
- ✅ "FastAPI后端从0到1" / "数据库设计从0到1" / "部署从0到1"

### Step 2：扫描项目代码

确保专栏例子来自用户真实项目——不是虚构的 demo。

### Step 3：先出 10 篇标题

让用户审阅标题再继续。标题格式：`第N篇 — 动词+名词 — 一句话吸引力`

### Step 4：逐篇或批量生成

- 生成全部 10 篇大纲（每篇 3-5 句摘要）
- 生成单篇完整内容（指定第几篇）

## 文件保存

```
docs/columns/{领域}-专栏大纲.md
docs/columns/{领域}-第{N}篇-{标题}.md
```

## 输出检查清单

- [ ] 每篇有一个前端类比
- [ ] 代码来自真实项目
- [ ] 至少 1 句阿珊金句
- [ ] 坑点表至少 3 条
- [ ] 保存到正确路径

---

## 附录：生图提示词模板

每篇文章配套 1-2 张配图，用于公众号/专栏封面或文中插图。以下按文章类型提供提示词模板。

### 画风统一设定

```
暖色调、扁平插画风格、中文标注、适合技术文章配图
配色：#F59E0B(琥珀主色) + #FFF8F0(暖白背景) + #1F2937(深色文字)
风格参考：Notion 插画 + 少数派文章配图
```

### 按文章类型的生图提示词

#### 概念认知类（第1篇）— 对比图

```
中文提示词：
一张左右对比的插画。左边是一个前端开发者看着浏览器里的localStorage数据消失（表情困惑），
右边是同一个开发者看着数据库服务器稳定运行（表情放松）。
暖色调扁平风格，画面简洁，适合技术专栏文章配图。
文字标注："localStorage: 浏览器清空就没了" vs "数据库: 数据持久保存"

English prompt:
A side-by-side illustration. Left side: a frontend developer looking confused as 
localStorage data disappears from a browser window. Right side: the same developer 
looking relaxed as a database server runs steadily. Warm color palette, flat 
illustration style, clean composition. Tech article illustration style.
Labels: "localStorage" vs "Database".
--ar 16:9 --style digital
```

#### 核心原理类（第2-3篇）— 架构图/结构图

```
中文提示词：
一张数据库表结构的可视化插画。一张"users"表以卡片形式展示，包含id、email、password_hash、
nickname、avatar、created_at等列，每列用不同颜色标识。旁边用前端代码注释的方式标注：
"这不就是一个大号 JSON 数组吗？" 暖色调，简洁技术风格。

English prompt:
A visual illustration of a database table structure. A "users" table shown as a card 
with columns: id, email, password_hash, nickname, avatar, created_at, each in different 
colors. Next to it, a code comment in frontend style: "Isn't this just a big JSON array?"
Warm color palette, clean tech illustration style. --ar 16:9
```

#### 操作实战类（第4篇）— 对照翻译图

```
中文提示词：
一张"前端代码 vs SQL语句"的对照翻译插画。左边是JavaScript代码块（array.filter, array.push,
array.splice），右边是等价的SQL语句（SELECT, INSERT, DELETE），中间用箭头连接。
每对操作展示为一行，共4行（增删改查）。暖色调，代码编辑器风格配色。

English prompt:
A translation comparison illustration: "JavaScript vs SQL". Left side shows JavaScript 
code (array.filter, array.push, array.splice), right side shows equivalent SQL 
(SELECT, INSERT, DELETE), connected by arrows. 4 rows for CRUD operations. 
Warm tone, code-editor color scheme, clean layout. --ar 16:9
```

#### 关系模型类（第5篇）— ER 图/关系图

```
中文提示词：
一张三张表的关联关系插画。User表在中间，Transaction表和Budget表在两侧，用箭头连线标注
外键关系（user_id）。每张表用卡片呈现，主键标金色，外键标蓝色。
标题："金橘记账的3张表"，暖色调，简洁数据库ER图风格。

English prompt:
An entity-relationship diagram illustration showing 3 tables. User table in the center, 
Transaction and Budget tables on the sides, connected by arrows showing foreign key 
relationships (user_id). Each table as a card, primary keys in gold, foreign keys in blue.
Title: "Finance Tracker Database Schema". Warm tone, clean ER diagram style. --ar 16:9
```

#### 原理揭示类（第6篇）— 翻译层示意图

```
中文提示词：
一张"ORM翻译层"的插画。左边是Python代码（db.query(User).filter(...)），经过中间一个
齿轮/翻译器（标注"SQLAlchemy"），右边输出SQL语句（SELECT * FROM users WHERE...）。
像一个翻译机器在工作。暖色调，扁平风格，适合技术文章。

English prompt:
An illustration of an "ORM Translation Layer". Python code on the left 
(db.query(User).filter(...)), passing through a gear/translator in the middle 
(labeled "SQLAlchemy"), outputting SQL on the right (SELECT * FROM users WHERE...).
Like a translation machine at work. Warm tone, flat tech illustration. --ar 16:9
```

#### 踩坑经验类（第7篇）— 警示/清单图

```
中文提示词：
一张"数据库10大坑点"的清单式插画。10个坑点以卡片网格排列，每个卡片上有一个emoji图标+
简短标题（如"💸 金额用float精度丢失"、"🔗 外键约束报错"）。最上方一个红色警示牌写着
"前端转全栈必看"。暖色调底，卡片白色，文字深色，简洁排版。

English prompt:
A "Top 10 Database Pitfalls" checklist illustration. 10 pitfall cards in a grid layout, 
each with an emoji icon and short title. A red warning sign at top: "Must Read for 
Full-Stack Beginners". Warm background, white cards, dark text, clean typography. 
--ar 3:4
```

#### 性能优化类（第8篇）— 速度对比图

```
中文提示词：
一张"有索引 vs 无索引"的速度对比插画。分左右两栏，左边"无索引"：小人爬一座高山（1000ms），
右边"有索引"：小人坐电梯直达（1ms）。中间标注"速度提升 1000 倍"。
暖色调，扁平插画，简洁有趣。

English prompt:
A speed comparison illustration: "With Index vs Without Index". Left side: a person 
climbing a tall mountain (labeled 1000ms). Right side: a person taking an elevator 
straight up (labeled 1ms). Center text: "1000x Faster". Warm tone, flat illustration, 
clean and playful style. --ar 16:9
```

#### 面试准备类（第9篇）— 问答卡片图

```
中文提示词：
一张"数据库面试10问"的知识卡片插画。10张卡片从左上到右下排列，每张卡片上一个问号+简短问题。
最上方标题"面试官会问什么？"。一张高亮卡片展开显示"答：概念+项目经验+踩坑故事"的公式。
暖色调，卡片式排版，简洁专业。

English prompt:
A "Top 10 Database Interview Questions" knowledge card illustration. 10 cards arranged in 
a grid, each with a question mark and short question. Top title: "What Will Interviewers 
Ask?" One highlighted card expanded showing the answer formula. Warm tone, card layout, 
clean professional style. --ar 3:4
```

#### 学习路线类（第10篇）— 路线图

```
中文提示词：
一张"数据库学习进阶路线图"插画。从左到右三条路径分叉：全栈开发（FastAPI+PG图标）、
数据分析（Pandas+SQL图标）、爬虫工具（数据采集+存储图标）。起点是一个前端开发者小人，
终点是三扇门分别通向不同方向。暖色调，路线图风格，清晰标注。

English prompt:
A "Database Learning Roadmap" illustration. Three branching paths from left to right: 
Full-Stack Dev (FastAPI+PG icons), Data Analysis (Pandas+SQL icons), Web Scraping 
(Data Collection+Storage icons). A frontend developer figure at the start, three doors 
at the end. Warm tone, roadmap style, clear labels. --ar 16:9
```

### 通用参数

| 用途 | 平台 | 参数 |
|------|------|------|
| 文章封面 | Midjourney | `--ar 16:9 --style digital --v 6` |
| 文中配图 | Midjourney | `--ar 4:3 --style digital --v 6` |
| 小红书/公众号头图 | Midjourney | `--ar 3:4 --style digital --v 6` |
| 免费替代 | DALL-E 3 | 用中文提示词即可，不需要参数 |

### 每篇文章生成时自动输出

在每篇文章末尾附上：
```markdown
## 🎨 本文配图提示词

**封面图（16:9）：**
{英文 prompt}

**文中配图（4:3）：**
{英文 prompt}

**中文版（DALL-E 可用）：**
{中文 prompt}
```
