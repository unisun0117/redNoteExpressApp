# CLAUDE.md

本文件为 Claude Code（claude.ai/code）在当前仓库中工作提供指引。

## 工作区布局

这是一个多项目工作区。当前主力开发项目是 **redNoteExpressApp** —— 一个基于 AI 的小红书风格文章生成器。

```
AI_Workspace/
├── redNoteExpressApp/    ← 主力项目（当前开发分支：ruflo）
│   ├── backend/          ← FastAPI（Python 3.10）
│   ├── frontend/         ← React 19 + TypeScript 6 + Vite 8
│   └── openspec/         ← 设计文档 & 变更提案
├── superpowers/          ← 上游 Claude Code 技能插件（仅同步，不要在这里改）
├── ruflo/                ← Claude Code 多智能体配置 & 技能
└── .claude/skills/       ← 用户本地技能覆盖（会话启动时加载）
```

## RedNote Express App — 架构

**技术栈：** React 19（Vite 8, antd-mobile 5）→ FastAPI → DeepSeek API（OpenAI 兼容协议）

### 后端（`redNoteExpressApp/backend/`）

```
app/
├── main.py              # FastAPI 入口、CORS、路由注册
├── config.py            # pydantic-settings — 所有配置项（环境变量 + 默认值）
├── database.py          # SQLAlchemy 引擎 + sessionmaker（生产用 SQLite）
├── models/user.py       # 用户模型（id, email, tier, credits_remaining）
├── middleware/auth.py    # JWT Bearer 鉴权依赖（get_current_user, require_vip）
├── routers/             # API 路由模块
│   ├── auth.py          # /api/auth/register, login, refresh, me
│   ├── generate.py      # /api/generate — POST，multipart form（图片 + 参数）
│   ├── styles.py        # /api/styles — 返回模板和赛道列表（公开接口）
│   ├── viral.py         # /api/viral/analyze — VIP 专属，桩实现
│   ├── billing.py       # /api/billing/* — 点数包、VIP 套餐（桩实现）
│   └── batch.py         # /api/batch/* — VIP 专属批量生成（桩实现）
├── services/generator.py # ArticleGenerator — 构建 prompts，通过 OpenAI SDK 调用 DeepSeek
└── wsgi.py              # PythonAnywhere WSGI→ASGI 桥接（asgiref）
```

**核心模式：**
- 使用 `openai.AsyncOpenAI`，`base_url` 指向 DeepSeek。API Key 放在 `OPENAI_API_KEY` 环境变量，不是单独的 DeepSeek key。
- 生成器返回 JSON 结构文章：`{title, intro, sections[{subtitle, content}], summary, store_info}`。
- 认证：JWT access + refresh 双 token，`passlib[bcrypt]`，bcrypt 版本必须为 4.0.1（5.x 与 passlib 不兼容）。
- 点数系统：每次 `/api/generate` 调用扣 1 个点数，耗尽返回 `402`。
- PythonAnywhere 免费版不支持原生 ASGI，必须通过 `asgiref` 做 WSGI 桥接。

### 前端（`redNoteExpressApp/frontend/`）

```
src/
├── main.tsx / App.tsx   # 入口、BrowserRouter、AuthProvider、PrivateRoute 路由守卫
├── services/api.ts      # 统一 HTTP 客户端 — authFetch 自动带 token + 401 自动刷新
├── hooks/useAuth.tsx    # React Context 管理认证状态（user, login, register, logout）
├── pages/
│   ├── GeneratorPage.tsx # 主页面 — 图片上传、关键词、风格配置、一键生成
│   ├── LoginPage.tsx     # 邮箱密码登录 + 注册
│   ├── DashboardPage.tsx # 个人中心 & 剩余点数
│   ├── BatchPage.tsx     # 多图批量生成（VIP）
│   └── BillingPage.tsx   # 点数包 & VIP 套餐
└── components/
    ├── StylePanel.tsx    # 模板/赛道下拉 + emoji/小标题开关
    ├── ImageUpload.tsx   # 图片上传 + 预览
    ├── ResultView.tsx    # 生成结果展示 + 一键复制
    ├── TabBar.tsx        # 底部导航栏（生成/批量/我的）
    └── ViralAnalyzer.tsx # 粘贴爆款文案链接/文本 分析风格
```

**核心模式：**
- `api.ts` 导出 `authFetch` 封装函数，自动附加 Bearer token，401 时自动刷新 token。需要鉴权的接口一律用 `authFetch`，不要直接用 `fetch`。
- StyleConfig 持久化在 `localStorage("style_config")`，与默认值合并使用。
- Vercel 构建命令用 `npx vite build`，不能用 `npm run build`（`tsc -b` 会因 `verbatimModuleSyntax` 报错）。

## 常用命令

### 后端（本地开发）
```bash
# 启动后端（一定要用 venv 里的 uvicorn，不能用系统全局的 Python）：
cd E:\AI_Workspace\redNoteExpressApp\backend
.\venv\Scripts\uvicorn.exe app.main:app --reload --port 8000

# 安装依赖（拉新代码后）：
.\venv\Scripts\pip.exe install -r requirements.txt
```

### 前端（本地开发）
```bash
cd E:\AI_Workspace\redNoteExpressApp\frontend
npm run dev          # 启动 Vite 开发服务器，localhost:5173
npm run lint         # ESLint 检查
npx vite build       # 生产构建（Vercel 用的就是这个命令）
```

### 部署
- **后端：** 推送到 `master` → PythonAnywhere 自动拉取（已配 web hook）。然后去 PythonAnywhere Web App 页面点 **Reload** 才能生效。
- **前端：** 推送到 `master` → Vercel 自动部署（构建根目录为 `redNoteExpressApp/frontend`）。
- **健康检查：** `https://huanglishan123.pythonanywhere.com/api/health`
- **API 文档：** `https://huanglishan123.pythonanywhere.com/docs`

### OpenSpec（项目管理）
本项目使用 OpenSpec 工作流，文档位于 `redNoteExpressApp/openspec/`。可用斜杠命令：
- `/opsx:explore` — 思路探索 & 问题分析
- `/opsx:propose` — 一键生成完整变更提案（设计 + 规格 + 任务）
- `/opsx:apply` — 按任务清单实施变更
- `/opsx:sync` — 将 delta spec 同步到主 spec
- `/opsx:archive` — 归档已完成的变更

## 常见坑点

1. **bcrypt 版本：** 必须是 `bcrypt==4.0.1`。5.x 会搞崩 passlib。venv 里已经是正确版本 — 务必用 `.\venv\Scripts\uvicorn.exe`，别用系统全局 uvicorn。
2. **PythonAnywhere 上 SQLite 路径：** `DATABASE_URL` 写 4 个斜杠：`sqlite:////home/huanglishan123/rednote.db`。
3. **Vercel 构建：** Build Command 填 `npx vite build`，别用 `npm run build`（TypeScript `verbatimModuleSyntax` 会导致 `tsc -b` 报错）。
4. **PythonAnywhere 免费版限制：** 不能连外部的 PostgreSQL，不支持原生 ASGI（必须 WSGI 桥接）。数据库文件必须放在 home 目录下。
5. **API Key 环境变量：** DeepSeek 的 key 配在 `OPENAI_API_KEY` 里 — OpenAI SDK 通过 `LLM_BASE_URL` 指向 DeepSeek。
6. **分支：** 生产分支是 `master`（不是 `main`）。Vercel 和 PythonAnywhere 都跟踪 `master`。
7. **Git push：** 国内网络到 GitHub 不稳定，超时就重试，或者用 PythonAnywhere 的 Bash 终端执行 git push。

## 环境变量

### 后端（`.env` 文件 或 PythonAnywhere WSGI 文件里配）
| 变量 | 默认值 | 说明 |
|---|---|---|
| `DATABASE_URL` | `sqlite:///./rednote.db` | PythonAnywhere 上用 4 个斜杠 |
| `JWT_SECRET` | — | 生产环境用 `rednote-super-secret-2026` |
| `OPENAI_API_KEY` | — | DeepSeek API Key |
| `LLM_BASE_URL` | `https://api.deepseek.com` | |
| `LLM_MODEL` | `deepseek-chat` | |
| `ALLOWED_ORIGINS` | `http://localhost:5173` | 逗号分隔，生产环境用 `*` |

### 前端（Vercel 环境变量）
| 变量 | 值 |
|---|---|
| `VITE_API_URL` | `https://huanglishan123.pythonanywhere.com/api` |

## 项目概述：红薯快写（RedNote Express App）

用户上传图片 + 输入关键词 + 选择风格/赛道 → AI 生成一篇完整的小红书风格文章。

文章结构：标题（18-20字）+ 前言（约50字）+ 3个段落（小标题 + 约100字正文）+ 总结（约50字）+ 店铺信息（约50字）。每次生成时，AI 会收到赛道人设指令、风格语气指令、以及 emoji/小标题开关。

**当前状态：** MVP 已上线运行，已实现：注册登录、单篇文章生成、风格赛道选择、本地开发调试。批量生成、爆款分析、支付集成目前是桩实现。线上地址：`https://red-note-express-app.vercel.app/login`。

## Ruflo 集成

工作区 `ruflo/` 目录包含 Claude Code 多智能体编排配置。处理跨多文件的复杂功能时：
- 可用 MCP 工具：`memory_store`、`memory_search`、`hooks_route`、`swarm_init`、`agent_spawn`
- 用 `ToolSearch("关键词")` 发现可用的 MCP 工具
- 通过 `SendMessage` 实现具名智能体协作（流水线/扇出/督导模式）
- 完整文档见 `ruflo/CLAUDE.md`

## 日常开发 — 真正帮你提效的 Claude Code 技能

开发红薯快写 App 时，用这些内置技能就够了（不需要额外配置，直接输入斜杠命令）：

| 场景 | 命令 | 说明 |
|------|------|------|
| 做新功能，先理清思路 | `/brainstorming` | AI 会引导你把需求想清楚再动手 |
| 写代码前先写测试 | `/test-driven-development` | 先写测试，再写实现 |
| 代码出 bug 找不到原因 | `/systematic-debugging` | 系统化排查，不是瞎猜 |
| 写完代码让 AI 审一遍 | `/code-review` | 检查是否有逻辑漏洞、代码质量问题 |
| 功能做完了要合并 | `/finishing-a-development-branch` | 帮你决定是合并还是提 PR |
| 让 AI 帮你做实施计划 | `/writing-plans` | 复杂任务先出计划，确认后再执行 |

### Ruflo（多智能体框架）

`ruflo/` 目录是一个第三方开源的多智能体框架。**日常开发不需要用它**。它适合的场景是：当项目非常复杂、一次要改几十个文件时，启动多个 AI 助手并行工作。对你当前阶段帮助有限，详见 `ruflo/CLAUDE.md`。

# skills

以后有新增 skills，都需要补充到汇总的 html

### 注意

不可以自动 push  需要我先 review 代码 确认 OK 了 再 push