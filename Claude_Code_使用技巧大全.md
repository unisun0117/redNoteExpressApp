# Claude Code 使用技巧大全 🚀

> 持续更新 · 最后整理：2026-07-02

---

## 目录

1. [快捷键速查](#1-快捷键速查)
2. [斜杠命令](#2-斜杠命令)
3. [提示词技巧](#3-提示词技巧)
4. [CLAUDE.md 项目记忆](#4-claudemd-项目记忆)
5. [Skills 技能系统](#5-skills-技能系统)
6. [Hooks 生命周期钩子](#6-hooks-生命周期钩子)
7. [Subagents 子智能体](#7-subagents-子智能体)
8. [Plan Mode 计划模式](#8-plan-mode-计划模式)
9. [权限与安全](#9-权限与安全)
10. [会话管理](#10-会话管理)
11. [并行工作](#11-并行工作)
12. [IDE 集成](#12-ide-集成)
13. [文件与图片上传](#13-文件与图片上传)
14. [上下文管理](#14-上下文管理)
15. [配置与环境](#15-配置与环境)
16. [Git 工作流](#16-git-工作流)
17. [多智能体编排](#17-多智能体编排)
18. [成本优化](#18-成本优化)
19. [日常开发工作流模板](#19-日常开发工作流模板)

---

## 1. 快捷键速查

### 1.1 全局快捷键

| 快捷键 | 功能 | 说明 |
|--------|------|------|
| `Ctrl+C` | 中断当前操作 | 终止 AI 输出或正在运行的任务 |
| `Ctrl+D` | 退出 Claude Code | 关闭会话 |
| `Ctrl+T` | 切换任务列表 | 查看/收起侧边任务面板 |
| `Ctrl+O` | 切换对话记录 | 查看完整对话历史 |
| `Ctrl+R` | 搜索历史命令 | 快速找回之前输入过的指令 |
| `Ctrl+B` | **后台运行命令** 🔥 | 把 `!` 开头的长时间命令放到后台，继续聊天 |
| `Ctrl+Shift+B` | 切换简要模式 | 精简/详细输出切换 |
| `Ctrl+]` | 打开 Artifact | 在浏览器中预览生成的 HTML/前端作品 |

### 1.2 聊天输入区

| 快捷键 | 功能 | 说明 |
|--------|------|------|
| `Alt+V` | **粘贴剪贴板图片** 🔥 | 从剪贴板直接粘贴截图，无需保存文件 |
| `Ctrl+J` | 换行 | 输入多行内容 |
| `Enter` | 发送消息 | 提交当前输入 |
| `Shift+Tab` | 切换模式 | 循环切换：默认 → 接受编辑 → 计划 → 自动 |
| `Ctrl+S` | 暂存当前输入 | 保存输入内容，稍后恢复 |
| `Ctrl+L` | 清空输入框 | 一键清除当前输入 |
| `Ctrl+G` / `Ctrl+X Ctrl+E` | 外部编辑器 | 在系统默认编辑器（vim/vscode）中编辑长文本 |
| `Ctrl+K` | 剪切到行尾 | 删除光标到行尾的内容 |
| `Ctrl+U` | 删除整行 | 一键清空当前行 |
| `Ctrl+_` / `Ctrl+-` | 撤销 | 撤销输入框中的上一步操作 |
| `Escape` | 中断/取消 | 中断 Claude 输出，或取消当前输入 |
| `Ctrl+X Ctrl+K` | 终止子智能体 | 强制停止所有正在运行的子智能体 |
| `Cmd+K` (macOS) | 清屏 | 清除聊天显示区域 |
| `Meta+P` | 模型选择器 | 快速切换模型 |
| `Meta+O` | 快速模式开关 | 切换快速/深度推理模式 |
| `Meta+T` | 思考模式开关 | 切换是否显示 AI 思考过程 |
| `Meta+W` | Workflow 关键词 | 切换 workflow 触发关键词 |
| `Space` (长按) | **语音输入** 🎤 | 按住说话，松开发送，比打字快 3 倍 |
| `↑` / `↓` | 历史提示词 | 浏览之前输入过的内容 |

### 1.3 自动补全

| 快捷键 | 功能 |
|--------|------|
| `Tab` | 接受补全建议 |
| `Escape` | 关闭补全菜单 |
| `↑` / `↓` | 上下浏览补全选项 |

### 1.4 确认对话框

| 快捷键 | 功能 |
|--------|------|
| `Y` / `Enter` | 确认 |
| `N` / `Escape` | 拒绝 |
| `Space` | 切换选项开关 |
| `Ctrl+E` | 展开详细说明 |

### 1.5 对话记录浏览

| 快捷键 | 功能 |
|--------|------|
| `Ctrl+E` | 展开/折叠完整内容 |
| `Ctrl+C` / `Escape` / `Q` | 退出记录浏览 |
| `↑` / `↓` / `J` / `K` | 上下滚动 |
| `G` / `Home` | 跳到开头 |
| `Shift+G` / `End` | 跳到底部 |
| `Ctrl+U` | 上半页 |
| `Ctrl+D` | 下半页 |
| `Space` | 整页下翻 |
| `Ctrl+B` | 整页上翻 |

### 1.6 Diff 对话框

| 快捷键 | 功能 |
|--------|------|
| `←` / `→` | 切换 diff 源（原始/修改后） |
| `↑` / `↓` / `J` / `K` | 切换文件 |
| `Enter` | 查看详情 |
| `Escape` | 关闭 |

### 1.7 消息选择器（时光倒流）

| 快捷键 | 功能 |
|--------|------|
| `Esc Esc` (快速双击) | **打开时光倒流** 🔥 |
| `↑` / `↓` / `J` / `K` | 选择历史消息 |
| `Enter` | 确认回退到该消息 |
| `Ctrl+↑` / `Shift+↑` | 跳到最早消息 |
| `Ctrl+↓` / `Shift+↓` | 跳到最新消息 |

### 1.8 底部状态栏

| 快捷键 | 功能 |
|--------|------|
| `↑` / `↓` / `Ctrl+P` / `Ctrl+N` | 浏览状态栏项目 |
| `←` / `→` | 切换上一项/下一项 |
| `Enter` | 打开选中项 |
| `Escape` | 清除选择 |
| `X` | 关闭弹窗 |

### 1.9 自定义快捷键

编辑 `~/.claude/keybindings.json`：

```json
{
  "$schema": "https://www.schemastore.org/claude-code-keybindings.json",
  "bindings": [
    {
      "context": "Chat",
      "bindings": {
        "ctrl+e": "chat:externalEditor",
        "ctrl+g": null
      }
    }
  ]
}
```

- 可用的上下文（context）：`Global`, `Chat`, `Autocomplete`, `Confirmation`, `Help`, `Transcript`, `HistorySearch`, `Task`, `ThemePicker`, `Settings`, `Tabs`, `Attachments`, `Footer`, `MessageSelector`, `DiffDialog`, `ModelPicker`, `Select`, `Plugin`, `Scroll`, `Doctor`
- 不可重新绑定的键：`Ctrl+C`, `Ctrl+D`, `Ctrl+M`, `CapsLock`
- macOS 系统保留：`Cmd+C/V/X/Q/W/Tab/Space`

---

## 2. 斜杠命令

### 2.1 内置命令速查

| 命令 | 功能 | 使用场景 |
|------|------|----------|
| `/help` | 查看帮助 | 忘记命令时 |
| `/clear` | 清空对话 | 切换新话题 |
| `/compact` | **压缩上下文** 🔥 | 上下文使用 70-80% 时主动执行 |
| `/context` | 查看上下文使用情况 | 彩色网格展示各部分占用 |
| `/config` | 打开配置面板 | 修改主题、输出风格、权限 |
| `/model` | 切换模型 | Opus/Sonnet/Haiku 之间切换 |
| `/effort` | 设置推理深度 | low/medium/high/xhigh/max/ultracode/auto |
| `/permissions` | 管理权限 | 预批准安全命令 |
| `/memory` | 管理记忆 | 保存偏好到项目记忆文件 |
| `/statusline` | 配置状态栏 | 显示模型、目录、分支、上下文用量 |
| `/color` | 更换输入颜色 | 区分并行会话窗口 |
| `/voice` | 语音输入 | 按住 Space 说话 |
| `/keybindings` | 查看快捷键 | 快捷键速查 |
| `/doctor` | 诊断工具 | 检查配置问题 |
| `/ide` | IDE 集成 | 连接 VS Code / JetBrains |
| `/add-dir` | 添加工作目录 | 给 Claude 访问多个仓库 |
| `/rewind` | **时光倒流** 🔥 | 回退到之前的对话状态 |
| `/branch` (或 `/fork`) | 分支对话 | 尝试不同方案而不丢失原路径 |
| `/loop` | 定时循环 | 每隔 N 分钟执行一次任务 |
| `/schedule` | 云端定时任务 | 关掉电脑也能跑 |
| `/sandbox` | 沙箱模式 | 隔离文件与网络 |
| `/btw` | 旁白提问 | 不打断当前工作的侧问 |
| `/goal` | 设置持久目标 | 跨轮次持续追踪目标 |
| `/powerup` | 交互式教程 | 学习 Claude Code 功能 |
| `/simplify` | 代码简化 | 检查代码质量和复用性 |
| `/run` | 运行应用 | 启动项目并验证改动 |
| `/review` | 代码审查 | 审查当前改动 |
| `/security-review` | 安全审查 | 安全漏洞检查 |
| `/init` | 初始化项目 | 为新项目生成 CLAUDE.md |
| `/verify` | 验证改动 | 端到端验证改动效果 |
| `/cost` | 查看费用 | 显示 token 用量和会话费用 |
| `/usage` | 查看配额 | 显示 API 用量限额和剩余额度 |
| `/diff` | 查看改动 | 显示本次会话所有的 git diff |
| `/copy` | 复制回复 | 把 Claude 上一次回复复制到剪贴板 |
| `/export` | 导出对话 | 导出完整对话为 Markdown 文件 |
| `/resume [name]` | 恢复会话 | 按名称或编号恢复之前的会话 |
| `/todos` | 任务清单 | 跨会话持久化的任务列表 |
| `/fast` | 快速模式 | 切换快速推理模式（适合简单任务） |
| `/vim` | Vim 模式 | 切换 Vim 键位绑定 |
| `/hooks` | 查看 Hooks | 显示当前的 Hooks 配置 |
| `/agents` | 管理子智能体 | 列出/创建/编辑子智能体 |
| `/skills` | 列出技能 | 查看所有可用的 Skill |
| `/mcp` | MCP 管理 | 添加/删除/配置 MCP 服务器 |
| `/worktree [name]` | 工作树管理 | 创建/进入/退出 git worktree |
| `/feedback` (或 `/bug`) | 反馈 | 向 Anthropic 提交 bug 或功能建议 |
| `/teleport <id>` | 迁移会话 | 把云端/网页会话迁移到本地终端 |
| `/remote-control` | 远程控制 | 从手机或浏览器控制本地会话 |

### 2.2 自定义斜杠命令

放在 `.claude/commands/` 目录下，一个 `.md` 文件就是一个命令：

```markdown
<!-- .claude/commands/review.md -->
---
description: 审查暂存区改动，检查安全、测试和规范
argument-hint: "[pr-number]"
allowed-tools: Read, Bash(git:*)
model: claude-opus-4-8
---

请分析 git diff 并检查：
1. 安全漏洞（注入、XSS 等）
2. 是否缺少测试
3. 是否符合 $ARGUMENTS 编码规范
```

**命名建议：** 用前缀分组，如 `review-`, `gen-`, `fix-`, `deploy-`，方便 `/` 后自动补全发现。

### 2.3 项目专属 Skills（技能）

技能放在 `.claude/skills/<name>/SKILL.md`，Claude 会根据任务描述自动调用：

```yaml
---
description: 写 React 组件时自动应用编码规范
effort: low
allowed-tools: Read, Write, Edit, Glob, Grep
---

# React 编码规范

## 必须遵守
- 使用函数组件 + Hooks
- Props 定义 TypeScript 接口
- 一个文件只导出一个组件

## 不能做
- 禁止使用 class 组件
- 禁止使用 any 类型
- 禁止在 useEffect 中直接写异步函数
```

---

## 3. 提示词技巧

### 3.1 核心心法 🔥

> **让 Claude 能自己验证自己的输出。**  
> 如果 Claude 能闭环验证，它会自己迭代到正确为止。

- **前端开发：** 安装 Claude Code Chrome 扩展，Claude 能看到页面渲染效果
- **后端开发：** 用 Desktop App 内置浏览器，Claude 能发请求验证 API
- **通用：** 告诉 Claude "写完自己跑一遍测试确认通过"

### 3.2 高效提示词模板

| 场景 | 提示词 |
|------|--------|
| 严格审查 | "Grill me on these changes"（狠狠审查我的改动） |
| 要求证明 | "Prove to me this works"（证明给我看这能跑通） |
| 推倒重来 | "Scrap this and implement the elegant solution"（扔掉这个，实现优雅方案） |
| 多用子智能体 | "use subagents"（在请求末尾加上这句） |
| 更新记忆 | "Update your CLAUDE.md so you don't make that mistake again" |

### 3.3 `!` + `@` 组合技 🔥

这是 Claude Code 最强大的日常操作模式：

```
! git status                                    # 先看状态（不走 LLM，零 token 消耗）
@src/components/Button.tsx                      # 引用文件（自动补全路径）
重构这个组件，使用新的设计 token              # 下达指令
```

**为什么高效：**
- `!` 命令直接执行，不走 AI，输出自动注入上下文
- `@` 一键引用，不用手动复制粘贴文件内容
- 零手动上下文准备，Claude 直接理解当前状态

### 3.4 高效沟通习惯

1. **`!` 前缀直接执行命令：** 输入 `!git diff --cached` 即可执行 shell 命令，输出直接注入对话上下文
2. **`@` 文件引用：** 输入 `@` + 文件名，自动补全路径，文件内容被注入上下文
3. **先计划再执行：** 复杂任务先让 Claude 出计划，确认后再动手
4. **新话题开新会话：** "AI 上下文像牛奶——新鲜冷藏最好"
5. **把 Claude 当成新同事：** 给足上下文（技术栈、文件结构、约束条件）
6. **主动 `/compact`：** 在上下文用量 50-70% 时主动压缩，不要等到 95%

---

## 4. CLAUDE.md 项目记忆

### 4.1 什么是 CLAUDE.md

项目根目录的 `CLAUDE.md` 是 Claude Code 的"项目入职文档"，**每次会话启动时自动加载**。

CLAUDE.md 有三个层级：

| 层级 | 路径 | 作用域 |
|------|------|--------|
| 全局 | `~/.claude/CLAUDE.md` | 所有项目通用 |
| 项目（隐藏） | `.claude/CLAUDE.md` | 当前项目，可通过 Git 共享 |
| 项目（根目录） | `CLAUDE.md`（仓库根目录） | **最常用**，Claude 主要读取这个 |

### 4.2 应该写什么

```markdown
# CLAUDE.md

## 技术栈
- 前端：React 19 + TypeScript + Vite
- 后端：FastAPI + SQLAlchemy + SQLite

## 项目结构
src/
├── pages/       # 页面组件
├── components/  # 可复用组件
├── services/    # API 调用层
└── hooks/       # 自定义 Hooks

## 常用命令
- 启动开发服务器：npm run dev
- 运行测试：npm test
- 构建生产包：npx vite build

## 编码规范
- 组件用函数式 + Hooks
- API 调用统一用 services/api.ts 里的 authFetch
- 样式用 antd-mobile 组件

## 已知坑点
- bcrypt 必须用 4.0.1，5.x 会崩溃
- Vercel 构建用 npx vite build，不能用 npm run build
- PythonAnywhere 免费版不支持原生 ASGI
```

### 4.3 最佳实践

- **控制 200 行以内** —— 太长 AI 可能忽略后面的内容
- **模块化规则：** 把详细规范拆分到 `.claude/rules/` 目录，在 CLAUDE.md 里引用
- **持续迭代：** 每次 Claude 犯错，就说 "Update your CLAUDE.md so you don't make that mistake again"
- **团队共享：** `.claude/` 目录加入 Git 版本控制，全团队受益

### 4.4 记忆系统 (`/memory`)

除了 CLAUDE.md，Claude Code 还有持久化记忆系统，保存在 `~/.claude/projects/<project>/memory/`：

- 每个记忆一个文件，带 YAML frontmatter（name, description, metadata）
- 自动记录偏好、反馈、项目约定
- 跨会话持久化
- 用 `/memory` 命令管理

---

## 5. Skills 技能系统

### 5.1 Skill vs Command 区别

| | Command（命令） | Skill（技能） |
|---|---|---|
| 触发方式 | 用户手动 `/` 调用 | Claude 根据任务自动判断 |
| 存放位置 | `.claude/commands/` | `.claude/skills/<name>/` |
| 格式要求 | Markdown，YAML frontmatter 可选 | YAML frontmatter **必填** |
| 适用场景 | 显式工作流 | 自动化编码规范/领域知识 |

### 5.2 Skill 文件结构

```
.claude/skills/
├── coding-standards/
│   ├── SKILL.md          # 主文件，含 frontmatter
│   ├── examples/         # 示例代码
│   └── anti-patterns.md  # 反面模式参考
├── tdd-workflow/
│   └── SKILL.md
└── security-review/
    └── SKILL.md
```

### 5.3 Skill Frontmatter 配置项

```yaml
---
description: 一句话描述，Claude 用它判断何时激活此技能
effort: low            # low | medium | high — 推理深度
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
model: claude-opus-4-8 # 可选：覆盖默认模型
---
```

### 5.4 写作技巧

- **给具体示例** —— AI 从示例中学习比从抽象规则中学得更准
- **写反面教材** —— 加一个"绝对不能做"部分
- **单一职责** —— 一个 Skill 只做一件事
- **描述要精准** —— `description` 决定了触发时机，太宽泛会误触发

---

## 6. Hooks 生命周期钩子

### 6.1 什么是 Hooks

Hooks 是**确定性**的 shell 命令，在特定事件发生时 100% 执行。和 Skill 不同，Skill 是概率性 AI 行为。

### 6.2 可用事件

| 事件 | 触发时机 | 典型用途 |
|------|----------|----------|
| `PreToolUse` | 工具执行前 | 阻止危险操作、添加提醒 |
| `PostToolUse` | 工具执行成功后 | 自动格式化、lint、记录日志 |
| `PostToolUseFailure` | 工具执行失败后 | 错误通知 |
| `Stop` | Claude 响应结束时 | 检查遗留 debug 代码 |
| `UserPromptSubmit` | 用户发送消息时 | 预处理、注入上下文 |
| `PreCompact` | 上下文压缩前 | 保存重要状态 |
| `PostCompact` | 上下文压缩后 | 重新注入关键指令 |
| `SessionStart` | 会话开始/恢复时 | 环境初始化、加载动态上下文 |
| `SessionEnd` | 会话终止时 | 清理、写交接文档 |
| `Notification` | 权限请求时 | 自定义提醒 |
| `PermissionDenied` | 权限被拒绝时 | 通知用户，可设 `retry: true` |
| `FileChanged` | 监控的文件变化时 | 响应外部编辑 |
| `SubagentStart/Stop` | 子智能体生命周期 | 监控并行任务 |
| `ConfigChange` | 配置文件变更时 | 审计追踪 |

### 6.3 Hook 类型

| 类型 | 说明 |
|------|------|
| `command` | Shell 命令（最常用） |
| `http` | 发送 HTTP 请求到外部服务 |
| `mcp_tool` | 调用 MCP 工具 |
| `prompt` | 让 LLM 评估判断 |
| `agent` | 派发子智能体处理 |

### 6.4 Hook 返回值约定

| 退出码 | 含义 |
|--------|------|
| `0` | 允许/继续 |
| `2` | 阻止操作 |
| 其他非零 | 记录为错误，但继续执行 |

### 6.5 配置示例

在 `.claude/settings.json` 中：

```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "command": "timeout 10 npx prettier --write \"$CLAUDE_FILE_PATH\" 2>/dev/null || true"
      }
    ],
    "Stop": [
      {
        "command": "grep -rn 'console.log' src/ --include='*.ts' && echo '⚠️ 发现 console.log 残留！' || true"
      }
    ],
    "PreToolUse": [
      {
        "matcher": "Edit|Write",
        "command": "if echo \"$CLAUDE_FILE_PATH\" | grep -qE '\.env$|secrets/'; then echo '⛔ 禁止修改敏感文件' && exit 1; fi"
      }
    ]
  }
}
```

### 6.4 Hooks 黄金法则

1. **一定加 `timeout N`** —— 防止钩子卡死会话
2. **一定加 `|| true`** —— 防止钩子失败导致 Claude 崩溃
3. **只做轻量操作** —— lint、格式化、快速检查；不要跑构建或完整测试套件
4. **先手动测试** —— 在启用前手动跑一遍钩子命令

---

## 7. Subagents 子智能体

### 7.1 概念

子智能体是独立上下文窗口中的 AI 助手，可以并行执行任务，不污染主会话上下文。

### 7.2 定义子智能体

放在 `.claude/agents/` 目录：

```yaml
# .claude/agents/code-reviewer.md
---
name: code-reviewer
description: 代码审查专家，检查逻辑漏洞和代码质量
model: claude-opus-4-8
effort: high
tools: Read, Glob, Grep, Bash
color: "#FF6B6B"
---
你是一个代码审查专家。你的任务是：
1. 检查代码逻辑是否正确
2. 发现潜在的性能问题
3. 验证是否遵循项目编码规范
4. 以结构化方式输出审查结果
```

### 7.3 使用方式

```bash
# 命令行指定 agent
claude --agent=code-reviewer "review src/auth.ts"

# 在对话中让 Claude 自己派发子智能体
"审查整个项目的安全性，use subagents"
```

### 7.4 常用子智能体模板

| 子智能体 | 用途 |
|----------|------|
| `code-reviewer` | 代码审查，查 bug 和质量 |
| `test-generator` | 自动生成单元测试 |
| `security-auditor` | 安全审计 |
| `doc-writer` | 写文档和注释 |
| `explorer` | 只读搜索，了解代码库 |
| `simplifier` | 代码简化和重构 |

### 7.5 高级用法

- **Worktree 隔离：** `isolation: "worktree"` —— 子智能体在独立 git worktree 中工作
- **限制步数：** `max_turns: 10` —— 防止探索型子智能体跑飞
- **并行派发：** 多个子智能体可同时运行

---

## 8. Plan Mode 计划模式

### 8.1 模式切换

按 `Shift+Tab` 循环切换：

```
默认模式 → 接受编辑(acceptEdits) → 计划模式(plan) → 自动模式(auto)
```

### 8.2 计划模式工作流 🔥

1. **进入计划模式** —— Claude 会先探索代码库、设计方案
2. **审视计划** —— 确认方案是否合理，提出修改意见
3. **退出计划模式** —— 确认后 Claude 开始实施
4. **切换到自动模式** —— 让 Claude 自动执行编辑，无需逐次确认

### 8.3 适用场景

- 新功能开发（先设计再实现）
- 跨多文件的架构变更
- 不确定最佳实现方式时
- 需要团队 review 方案时

---

## 9. 权限与安全

### 9.1 权限模式

| 模式 | 说明 |
|------|------|
| 默认 | 每个操作都要确认 |
| 自动模式 | Claude 自行判断，高风险操作仍会标识 |
| 计划模式 | 仅允许只读操作 |
| YOLO 模式 | 完全不询问（不推荐） |

### 9.2 预批准常用命令

用 `/permissions` 或在 `settings.json` 中配置：

```json
{
  "permissions": {
    "allow": [
      "Bash(npm run *)",
      "Bash(npx vite *)",
      "Bash(git diff *)",
      "Bash(git status)",
      "Edit(src/**)",
      "WebFetch(domain:localhost)"
    ]
  }
}
```

### 9.3 安全建议

- 永远不要把 `.env`、密钥文件加到 allow 列表
- 敏感目录用 hooks 加保护
- 用 `/sandbox` 隔离高风险操作

---

## 10. 会话管理

### 10.1 会话操作

| 操作 | 方法 |
|------|------|
| 恢复上次会话 | 直接运行 `claude`，自动检测未完成会话 |
| 查看会话列表 | `claude --resume` |
| 新会话 | `/clear` 或 `claude --new` |
| 压缩上下文 | `/compact`（建议 50-70% 时主动做） |
| 时光倒流 | `Esc Esc` 或 `/rewind` |
| 分支对话 | `/branch` 或 `/fork` |
| 跨设备迁移 | `claude --teleport` |

### 10.2 时光倒流 (`/rewind`) 🔥

按 `Esc Esc` 打开消息选择器 → 选择历史消息 → 回到那个状态重来。

三种回退方式：
- 仅恢复代码
- 仅恢复对话
- 两者都恢复

### 10.3 分支对话 (`/branch`)

在某个节点分叉出两条路径：
- 一条继续当前方向
- 另一条尝试替代方案
- 两条路径互不影响

---

## 11. 并行工作

### 11.1 多窗口并行

```bash
# 终端 1
claude --worktree --name feature-a

# 终端 2
claude --worktree --name feature-b

# 终端 3
claude --worktree --name refactor-c
```

每个 worktree 是一个独立的 git 工作区，互不干扰。

### 11.2 区分并行会话

```bash
/color red      # 终端 1 用红色
/color blue     # 终端 2 用蓝色
/color green    # 终端 3 用绿色
```

### 11.3 Tmux 集成

```bash
claude --worktree --tmux   # 自动创建 tmux 会话
```

### 11.4 批量并行 (`/batch`)

适合大规模迁移，派发数十上百个子智能体，每个在隔离环境中工作，各自测试和提交 PR。

---

## 12. IDE 集成

### 12.1 VS Code

- 安装 Claude Code VS Code 扩展
- 在 VS Code 中打开的文件，Claude 可以直接感知
- Claude 编辑的文件会自动在 VS Code 中刷新

### 12.2 JetBrains

- 安装 Claude Code JetBrains 插件
- 支持 IntelliJ IDEA、WebStorm、PyCharm 等

### 12.3 Chrome 扩展 🔥

- Claude 能看到浏览器中的页面渲染效果
- 前端开发利器：Claude 写了代码 → 看到效果 → 自己调整

### 12.4 Desktop App

- 内置浏览器，适合 Web 应用开发
- 支持 `/remote-control` 远程控制

---

## 13. 文件与图片上传

### 13.1 方式汇总

| 方式 | 操作 | 适用场景 |
|------|------|----------|
| `Alt+V` 🔥 | 粘贴剪贴板图片 | 截图后直接粘贴，最快 |
| `@文件名` | 引用文件 | 引用项目中的代码文件 |
| 拖拽 | 拖文件到终端 | 上传图片/PDF/文档 |
| 复制粘贴 | 复制文件路径粘贴 | Windows/Linux |
| `Ctrl+V` | 粘贴剪贴板内容 | 粘贴文本内容 |

### 13.2 支持的文件类型

- 图片：PNG、JPG、GIF、WebP、SVG
- 文档：PDF（最多 20 页/次）
- 代码：所有文本文件
- Notebook：Jupyter `.ipynb`

### 13.3 `@` 文件引用技巧

- 输入 `@` 自动弹出路径补全
- 支持模糊匹配
- 文件内容直接注入上下文

---

## 14. 上下文管理

### 14.1 核心策略

| 策略 | 说明 |
|------|------|
| `/compact` 提前做 | 50-70% 时主动压缩，不要等到 95% |
| `/context` 可视化 | 查看各类内容占用比例 |
| 子智能体分担 | 大量中间输出的工作交给子智能体 |
| 新话题开新会话 | 上下文是消耗品，不是无限资源 |
| 禁用无用 MCP | 只保留 5-6 个 MCP，减少 token 消耗 |

### 14.2 1M 上下文窗口

Claude Code 支持 100 万 token 上下文窗口，但：
- 长上下文会让响应变慢
- 信息密度越高越好
- 定期 `/compact` 保持上下文精简

---

## 15. 配置与环境

### 15.1 配置文件层级

```
优先级从高到低：
1. 企业管理配置（OS 特定路径）
2. ~/.claude/settings.json      （用户全局）
3. .claude/settings.json         （项目共享，提交到 Git）
4. .claude/settings.local.json   （个人覆盖，gitignore）
```

### 15.2 推荐的 `.claude/` 目录结构

```
.claude/
├── settings.json          # 团队共享设置 + hooks
├── settings.local.json    # 个人覆盖（gitignore）
├── commands/              # 自定义斜杠命令
│   ├── review.md
│   └── deploy.md
├── skills/                # AI 自动调用的技能
│   ├── coding-standards/
│   │   └── SKILL.md
│   └── tdd-workflow/
│       └── SKILL.md
├── agents/                # 子智能体定义
│   ├── code-reviewer.md
│   └── test-generator.md
├── rules/                 # 模块化规则
│   ├── security.md
│   └── coding-style.md
└── hooks/                 # Hook 脚本
    └── post-edit.sh
```

### 15.3 常用环境变量

```bash
# 终端设置（或放 .bashrc）
export CLAUDE_CODE_NO_FLICKER=1    # 无闪烁渲染
export ENABLE_LSP_TOOL=1           # 启用 LSP 工具（50ms 级别代码导航！）
```

### 15.4 无头模式与 CLI 参数

```bash
# === 一问一答（非交互） ===
claude -p "explain src/main.ts"                    # 单次问答
git diff | claude -p "review for bugs"             # 管道输入
claude -p "analyze" --output-format stream-json    # JSON 流式输出
claude --bare -p "quick question"                  # 快速启动（约快 10 倍）

# === 会话管理 ===
claude -c                          # 恢复最近一次会话
claude -r "feature-auth"           # 按名称恢复会话
claude --resume                    # 交互式选择要恢复的会话
claude --name "my-task"            # 给本次会话命名
claude --new                       # 强制开启全新会话
claude --teleport <session_id>     # 从其他设备迁移会话过来

# === 模型与推理 ===
claude --model opus                # 指定启动模型
claude --effort high               # 设置默认推理深度

# === 权限与安全 ===
claude --permission-mode plan      # 以只读计划模式启动
claude --allowedTools "Edit,Read"  # 限制可用工具
claude --enable-auto-mode          # 启用自动权限模式
claude --dangerously-skip-permissions  # 跳过所有确认（CI 环境用）
claude --no-mcp                    # 禁用所有 MCP 服务器

# === 并行开发 ===
claude --worktree feature-auth     # 创建独立 worktree 并进入
claude --worktree --tmux           # worktree + 自动创建 tmux 会话
claude --agent=code-reviewer       # 使用自定义子智能体

# === 调试 ===
claude --verbose                   # 显示调试信息
claude --max-turns 10              # 限制最大推理步数
```

---

## 16. Git 工作流

### 16.1 日常命令

```bash
# 让 Claude 审查暂存区
"审查 git staged 改动"

# 让 Claude 写 commit message
"根据 staged 改动生成 commit message"

# 让 Claude 处理 PR
"review this PR: <链接>"
```

### 16.2 PR 集成

- 在 PR 评论中 `@claude`，自动把经验教训加入 CLAUDE.md
- Claude 可以在 PR 上做 code review 并发表评论

### 16.3 Worktree 隔离开发

```bash
# 创建隔离工作区
claude --worktree

# 多个并行功能开发
claude --worktree --name feature-login
claude --worktree --name feature-payment
```

### 16.4 自动化 CI

```bash
# 定时自动 rebase
/loop 30m "检查主分支是否有新提交，帮我 rebase"

# 定时审查开放的 PR
/loop 1h "查看我负责的 PR 是否有新评论需要处理"
```

---

## 17. 多智能体编排

### 17.1 何时用多智能体

- 一次要改几十个文件
- 需要从多个维度同时审查代码
- 大规模迁移/重构
- 测试生成（每个文件一个子智能体）

### 17.2 Workflow 模式

| 模式 | 说明 |
|------|------|
| Pipeline（管道） | A→B→C，每个结果传给下一阶段 |
| Parallel（并行） | 多个任务同时跑，等全部完成后汇总 |
| Fan-out（扇出） | 一对多派发，各自独立完成 |

### 17.3 触发方式

- 请求末尾加 `use subagents`
- 用 `ultracode` effort level（自动启用多智能体编排）
- 手动调用 `/batch`

### 17.4 Agent Teams（实验性）🔬

启用方式：`CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1`

这是点对点多智能体协作模式：
- **共享任务列表** —— 带依赖追踪
- **文件锁** —— 防止写入冲突
- **Team Lead** —— 一个领队智能体负责派发和协调
- 适合复杂的多领域交叉任务
- 成本更高，但能处理单个智能体无法独立完成的工作

### 17.5 多智能体适用场景对比

| 模式 | 成本 | 适用场景 |
|------|------|----------|
| 单个子智能体 | 低 | 专注的委派任务、上下文隔离 |
| Agent Teams | 较高 | 并行多领域交叉工作 |
| Dynamic Workflows | 最高 | 大规模审计、迁移、代码库级重构 |

---

## 18. 成本优化 💰

### 18.1 省钱五原则

| 原则 | 操作方法 |
|------|----------|
| **模型分级** | 探索用 Haiku、编码用 Sonnet、架构设计用 Opus |
| **Shell 走捷径** | 用 `!` 前缀执行命令，绕过 LLM，不消耗 token |
| **快速模式** | 简单任务（语法、样板代码）用 `/fast` |
| **上下文精简** | 定期 `/compact`，新话题用 `/clear` |
| **关闭无用 MCP** | 只保留 5-6 个 MCP 服务器，减少 token 开销 |

### 18.2 费用监控

```bash
/cost           # 查看当前会话费用
/usage          # 查看 API 配额和剩余额度
/context        # 查看上下文占用比例
```

### 18.3 按场景选模型

| 场景 | 推荐模型 | 原因 |
|------|----------|------|
| 代码探索/搜索 | Haiku | 快、便宜、够用 |
| 日常编码 | Sonnet | 性价比最佳 |
| 复杂架构设计 | Opus | 深度推理 |
| 安全审计 | Opus + xhigh | 需要最严密的逻辑 |
| 批量生成 | Haiku 并行 | 数量换质量 |

---

## 19. 日常开发工作流模板

### 18.1 做新功能

```
1. /brainstorming           → 理清需求
2. Shift+Tab 进入计划模式    → 设计方案
3. 审视计划，确认无误         → 退出计划模式
4. Shift+Tab 进入自动模式    → Claude 自动编写代码
5. /verify                  → 端到端验证
6. /code-review             → 代码审查
7. /finishing-a-development-branch → 合并或提 PR
```

### 18.2 修 Bug

```
1. /systematic-debugging    → 系统化排查
2. 复述 bug 现象和上下文     → 给足信息
3. Claude 定位根因并修复     → 
4. /verify                  → 确认修复有效
```

### 18.3 写测试

```
1. /test-driven-development → 先写测试
2. Claude 生成测试用例      →
3. /run 或手动验证          → 看红灯
4. Claude 写实现代码         → 看绿灯
```

### 18.4 代码审查

```
1. 提交前：/code-review     → 自审
2. 提 PR 后：@claude 评论   → AI 审查
3. 合并前：/simplify        → 简化重构
```

---

## 附录：速查卡片

### 🔥 最常用的 10 个技巧

1. **`Alt+V`** — 粘贴剪贴板图片
2. **`Shift+Tab`** — 切换计划/自动模式
3. **`/compact`** — 在 50-70% 时主动压缩上下文
4. **`Esc Esc`** — 时光倒流，回到之前的状态
5. **`!command`** — 在聊天中直接执行 shell 命令
6. **`@filename`** — 快速引用文件
7. **`/effort max`** — 复杂任务开最大推理深度
8. **子智能体** — 并行处理多文件任务
9. **CLAUDE.md** — 持续迭代你的项目入职文档
10. **`claude --worktree`** — 并行功能开发互不干扰

### 📁 关键文件路径

| 文件 | 路径 |
|------|------|
| 项目配置 | `.claude/settings.json` |
| 项目记忆 | `CLAUDE.md` |
| 自定义命令 | `.claude/commands/*.md` |
| 自定义技能 | `.claude/skills/<name>/SKILL.md` |
| 子智能体 | `.claude/agents/*.md` |
| 快捷键配置 | `~/.claude/keybindings.json` |
| 用户全局配置 | `~/.claude/settings.json` |
| 记忆存储 | `~/.claude/projects/<project>/memory/` |

---

> **核心理念：** Claude Code 不只是一个聊天工具，它是一个完整的开发环境。投入时间配置 CLAUDE.md、Skills、Hooks，收益会随着使用次数指数增长。
