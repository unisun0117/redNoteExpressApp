# Superpowers 项目分析报告

## 项目概览

**项目名称：** Superpowers  
**作者：** Jesse Vincent (@obra)  
**GitHub 地址：** https://github.com/obra/superpowers  
**描述：** 一个完整的软件开发方法论，为编码代理（AI Coding Agents）提供可组合的技能框架

### 统计数据
- **Star 数：** 170,180
- **Fork 数：** 15,026
- **开源问题：** 282
- **主要语言：** Shell
- **许可证：** MIT
- **创建时间：** 2025-10-09
- **最后更新：** 2026-04-28

---

## 核心理念

Superpowers 是一个为 AI 编码代理设计的完整软件开发方法论，它不是简单的代码生成工具，而是一套系统化的开发流程。

### 工作原理

1. **需求澄清阶段** - 代理不会立即编写代码，而是先询问你真正想要实现什么
2. **规格设计** - 从对话中提炼出规格说明，分块展示便于阅读和消化
3. **实施计划** - 创建清晰的实施计划，强调 TDD（测试驱动开发）、YAGNI（你不会需要它）和 DRY（不要重复自己）原则
4. **子代理驱动开发** - 启动子代理处理各个工程任务，检查和审查工作，持续推进
5. **自主工作** - Claude 可以自主工作数小时而不偏离计划

---

## 核心工作流程

### 1. brainstorming（头脑风暴）
- 在编写代码前激活
- 通过提问细化粗略想法
- 探索替代方案
- 分段展示设计以供验证
- 保存设计文档

### 2. using-git-worktrees（使用 Git 工作树）
- 设计批准后激活
- 在新分支上创建隔离工作空间
- 运行项目设置
- 验证干净的测试基线

### 3. writing-plans（编写计划）
- 有了批准的设计后激活
- 将工作分解为小任务（每个 2-5 分钟）
- 每个任务包含确切的文件路径、完整代码和验证步骤

### 4. subagent-driven-development（子代理驱动开发）
- 有计划后激活
- 为每个任务分派新的子代理
- 两阶段审查：规格合规性，然后代码质量

### 5. test-driven-development（测试驱动开发）
- 实施期间激活
- 强制执行 RED-GREEN-REFACTOR 循环
- 编写失败测试 → 观察失败 → 编写最小代码 → 观察通过 → 提交

### 6. requesting-code-review（请求代码审查）
- 任务之间激活
- 根据计划审查
- 按严重程度报告问题

### 7. finishing-a-development-branch（完成开发分支）
- 任务完成时激活
- 验证测试
- 提供选项（合并/PR/保留/丢弃）
- 清理工作树

---

## 技能库

### 测试
- **test-driven-development** - RED-GREEN-REFACTOR 循环（包含测试反模式参考）

### 调试
- **systematic-debugging** - 4 阶段根因分析流程
- **verification-before-completion** - 确保问题真正修复

### 协作
- **brainstorming** - 苏格拉底式设计细化
- **writing-plans** - 详细实施计划
- **executing-plans** - 批量执行与检查点
- **dispatching-parallel-agents** - 并发子代理工作流
- **requesting-code-review** - 预审查清单
- **receiving-code-review** - 响应反馈
- **using-git-worktrees** - 并行开发分支
- **finishing-a-development-branch** - 合并/PR 决策工作流
- **subagent-driven-development** - 快速迭代与两阶段审查

### 元技能
- **writing-skills** - 遵循最佳实践创建新技能
- **using-superpowers** - 技能系统介绍

---

## 项目结构

```
superpowers/
├── .claude-plugin/          # Claude Code 插件配置
├── .codex-plugin/           # Codex 插件配置
├── .cursor-plugin/          # Cursor 插件配置
├── .opencode/               # OpenCode 配置
├── agents/                  # 代理相关文件
├── assets/                  # 资源文件
├── commands/                # 命令实现
├── docs/                    # 文档
├── hooks/                   # 钩子脚本
├── scripts/                 # 工具脚本
├── skills/                  # 技能库（核心）
├── tests/                   # 测试文件
├── CLAUDE.md               # Claude 行为指南
├── GEMINI.md               # Gemini 配置
├── README.md               # 项目说明
├── RELEASE-NOTES.md        # 发布说明
└── package.json            # 项目配置
```

---

## 支持的平台

Superpowers 支持多个 AI 编码代理平台：

1. **Claude Code** - 官方市场和 Superpowers 市场
2. **OpenAI Codex CLI** - 通过插件搜索
3. **OpenAI Codex App** - 通过应用内插件
4. **Cursor** - 通过插件市场
5. **OpenCode** - 通过 URL 安装
6. **GitHub Copilot CLI** - 通过市场安装
7. **Gemini CLI** - 通过扩展安装

---

## 设计哲学

1. **测试驱动开发** - 始终先编写测试
2. **系统化而非临时** - 流程优于猜测
3. **降低复杂性** - 简洁是首要目标
4. **证据而非声明** - 在宣布成功前先验证

---

## 安装示例

### Claude Code（官方市场）
```bash
/plugin install superpowers@claude-plugins-official
```

### Claude Code（Superpowers 市场）
```bash
/plugin marketplace add obra/superpowers-marketplace
/plugin install superpowers@superpowers-marketplace
```

### Cursor
```text
/add-plugin superpowers
```

### Gemini CLI
```bash
gemini extensions install https://github.com/obra/superpowers
```

---

## 社区与支持

- **Discord：** https://discord.gg/35wsABTejz
- **问题跟踪：** https://github.com/obra/superpowers/issues
- **发布通知：** https://primeradiant.com/superpowers/
- **赞助：** https://github.com/sponsors/obra

---

## 贡献指南

1. Fork 仓库
2. 切换到 'dev' 分支
3. 为你的工作创建分支
4. 遵循 `writing-skills` 技能创建和测试新技能或修改
5. 提交 PR，填写 PR 模板

**注意：** 项目不接受新技能的贡献，技能更新必须在所有支持的编码代理上工作。

---

## 总结

Superpowers 是一个革命性的 AI 编码代理框架，它将软件开发从"让 AI 写代码"提升到"让 AI 系统化地开发软件"。通过强制执行最佳实践（TDD、代码审查、计划驱动开发），它确保 AI 代理能够像经验丰富的工程师一样工作，而不仅仅是代码生成器。

该项目的高 Star 数（170K+）和活跃的社区表明它在 AI 辅助开发领域具有重要影响力。对于任何使用 AI 编码助手的开发者来说，Superpowers 都值得深入了解和使用。
