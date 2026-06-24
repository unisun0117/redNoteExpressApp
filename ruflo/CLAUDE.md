# Ruflo — 多智能体协作框架

## 这是什么？

Ruflo 是一个第三方开源项目（github.com/ruvnet/ruflo），它为 Claude Code 提供"多智能体协作"能力。简单说：当你要改很多文件、做复杂功能时，它可以同时启动多个 AI 助手分工合作——有人写代码、有人审代码、有人测代码。

**当前状态：** 已安装但大部分功能并未实际运行。你日常开发红薯快写 App 时，**不需要主动使用它**。

## 对你有什么用？

坦白说，对你当前阶段帮助有限。原因：

1. **你的项目是单人开发** — Ruflo 的多智能体模式适合 3-5 人团队协作的场景
2. **大部分功能需要额外付费的 API 调用** — 启动多个智能体会消耗大量 token
3. **它的技能主要是给 Ruflo 框架本身开发用的** — 不是给你的红薯快写 App 用的

## 什么时候可能有用？

- 将来红薯快写 App 功能变复杂，一次要改 10+ 个文件的时候
- 你想让 AI 写完代码后，另一个 AI 自动检查一遍
- 你要做大规模重构（比如整个后端从 FastAPI 迁移到别的框架）

## 目录说明

```
ruflo/
├── CLAUDE.md              ← 当前文件
├── ruflo.txt              ← 项目 GitHub 地址
├── ruflo.html / ruflo.png ← 项目介绍的网页截图
├── .claude-flow/          ← Ruflo 运行配置
│   ├── config.yaml        ← 集群配置（最大15个智能体等）
│   └── CAPABILITIES.md    ← 功能清单（英文原文）
├── .claude/
│   ├── agents/            ← 60+ 种智能体角色定义（程序员/审查员/测试员/架构师…）
│   ├── skills/            ← 30 个技能（都是给 Ruflo 框架开发用的）
│   └── commands/          ← 斜杠命令定义
```

## 配置要点

```yaml
# .claude-flow/config.yaml 的关键设置
swarm.maxAgents: 15     # 最多同时 15 个 AI 助手
memory.backend: hybrid  # 混合记忆模式（文件 + 向量搜索）
neural.enabled: true    # 启用自学习（记录成功/失败模式）
mcp.autoStart: false    # MCP 服务不自动启动
```

## 可用命令（供参考，日常不需要用）

```bash
# 启动多智能体集群
npx ruflo@latest swarm init --topology hierarchical --max-agents 8

# 启动一个子智能体
npx ruflo@latest agent spawn -t coder --name my-coder

# 诊断问题
npx ruflo@latest doctor --fix
```

## 真正对你有用的工具

开发红薯快写 App 时，你应该用的是 Claude Code 内置的技能，而不是 Ruflo：

| 场景 | 用什么 |
|------|--------|
| 开发新功能前先想清楚方案 | `/brainstorming` |
| 写代码前先写测试 | `/test-driven-development` |
| 代码出 bug 了 | `/systematic-debugging` |
| 写完代码让 AI 检查 | `/code-review` |
| 功能做完了合入主分支 | `/finishing-a-development-branch` |

这些才是真正帮你提效的工具，不需要额外配置，直接用就行。
