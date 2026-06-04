# OpenSpec 详细使用指南

## 目录

1. [快速入门](#快速入门)
2. [核心概念](#核心概念)
3. [工作流详解](#工作流详解)
4. [实战案例](#实战案例)
5. [命令参考](#命令参考)
6. [最佳实践](#最佳实践)
7. [常见问题](#常见问题)

---

## 快速入门

### 安装

**前置要求**：Node.js >= 20.19.0

```bash
# 全局安装
npm install -g @fission-ai/openspec@latest

# 或使用其他包管理器
pnpm add -g @fission-ai/openspec@latest
yarn global add @fission-ai/openspec@latest
bun add -g @fission-ai/openspec@latest
```

### 初始化项目

```bash
# 进入项目目录
cd your-project

# 初始化 OpenSpec
openspec init
```

初始化后会创建以下结构：

```
your-project/
├── openspec/
│   ├── specs/              # 规范源（系统行为的真实记录）
│   ├── changes/            # 变更提案（进行中的工作）
│   └── config.yaml         # 配置文件（可选）
├── CLAUDE.md               # AI 助手指令（如果使用 Claude）
└── AGENTS.md               # 通用 AI 助手指令
```

### 配置工作流

OpenSpec 提供两种工作流模式：

```bash
# 查看和选择工作流配置
openspec config profile

# 更新 AI 指令
openspec update
```

**两种模式**：

1. **Core 模式**（默认）- 快速路径
   - `/opsx:propose` - 创建变更和规划
   - `/opsx:apply` - 实现任务
   - `/opsx:archive` - 归档完成的变更

2. **Expanded 模式** - 完整控制
   - `/opsx:new` - 创建变更脚手架
   - `/opsx:continue` - 逐步创建工件
   - `/opsx:ff` - 快速创建所有规划工件
   - `/opsx:verify` - 验证实现
   - `/opsx:bulk-archive` - 批量归档

---

## 核心概念

### 1. 规范（Specs）

规范是系统行为的真实记录，位于 `openspec/specs/` 目录。

**规范结构**：

```markdown
# 认证规范

## 需求：用户登录
系统必须允许用户使用邮箱和密码登录。

### 场景：成功登录
- 给定：用户提供有效的邮箱和密码
- 当：用户提交登录表单
- 那么：系统创建会话并重定向到仪表板

### 场景：登录失败
- 给定：用户提供无效的凭据
- 当：用户提交登录表单
- 那么：系统显示错误消息
- 并且：不创建会话
```

### 2. 变更（Changes）

每个变更都有独立的文件夹，包含以下工件：

```
openspec/changes/add-dark-mode/
├── proposal.md      # 提案：为什么做、做什么
├── specs/           # 增量规范：添加/修改/删除的需求
│   └── ui/
│       └── spec.md
├── design.md        # 设计：技术方案
└── tasks.md         # 任务：实现清单
```

### 3. 增量规范（Delta Specs）

增量规范显示相对于当前规范的变化：

```markdown
# UI 增量规范

## 添加的需求

### 需求：主题选择
系统应允许用户在浅色和深色主题之间切换。

#### 场景：手动切换
- 给定：用户在任何页面
- 当：用户点击主题切换按钮
- 那么：主题立即切换
- 并且：偏好在会话间持久化

## 修改的需求

### 需求：会话超时
系统应在 30 分钟不活动后使会话过期。
（之前：60 分钟）

## 删除的需求

### 需求：记住我功能
（已弃用，改用 2FA）
```

### 4. 工件依赖关系

```
proposal ──► specs ──► design ──► tasks ──► implement
   ▲           ▲          ▲                    │
   └───────────┴──────────┴────────────────────┘
            在学习过程中更新
```

工件之间有依赖关系，但可以随时回退更新。

---

## 工作流详解

### Core 工作流（默认）

#### 1. 创建提案

```text
你：/opsx:propose add-user-profile

AI：已创建 openspec/changes/add-user-profile/
    ✓ proposal.md — 为什么做、做什么
    ✓ specs/       — 需求和场景
    ✓ design.md    — 技术方案
    ✓ tasks.md     — 实现清单
    准备好实现了！
```

#### 2. 实现任务

```text
你：/opsx:apply

AI：正在执行任务...
    ✓ 1.1 创建 UserProfile 组件
    ✓ 1.2 添加个人资料 API 端点
    ✓ 2.1 实现头像上传
    ✓ 2.2 添加表单验证
    所有任务完成！
```

#### 3. 归档变更

```text
你：/opsx:archive

AI：正在归档 add-user-profile...
    ✓ 将规范合并到 openspec/specs/user/spec.md
    ✓ 移动到 openspec/changes/archive/2026-05-03-add-user-profile/
    完成！准备好下一个功能。
```

### Expanded 工作流

#### 1. 探索阶段

当需求不明确时，先探索：

```text
你：/opsx:explore

AI：你想探索什么？

你：我想提升页面加载性能，但不确定瓶颈在哪里。

AI：让我帮你调查...
    [分析打包大小、识别慢查询、检查组件渲染模式]
    
    我发现了三个主要瓶颈：
    1. 大型未优化的图片
    2. ProductList 中的同步数据获取
    3. 上下文变化导致的重新渲染
    
    你想先解决哪个？

你：让我们处理数据获取问题。
```

#### 2. 创建变更

```text
你：/opsx:new optimize-product-list-fetching

AI：已创建变更。准备创建：proposal
```

#### 3. 逐步创建工件

```text
你：/opsx:continue

AI：基于我们的探索创建提案...
    [编写包含调查见解的提案]
    
    准备创建：specs, design
    你想要哪个？

你：/opsx:continue

AI：创建规范...
    ✓ specs/performance/spec.md
    
    准备创建：design
```

#### 4. 快速前进

如果需求清晰，可以跳过逐步创建：

```text
你：/opsx:ff

AI：创建所有规划工件...
    ✓ proposal.md
    ✓ specs/
    ✓ design.md
    ✓ tasks.md
    准备好实现了！
```

#### 5. 验证实现

```text
你：/opsx:verify

AI：验证 add-auth...
    
    完整性
    ✓ tasks.md 中的所有 12 个任务已完成
    ✓ 规范中的所有需求都有对应代码
    ⚠ 场景"不活动后会话超时"未测试
    
    正确性
    ✓ 实现符合规范意图
    ✓ 处理了场景中的边缘情况
    ✓ 错误状态符合规范定义
    
    一致性
    ✓ 设计决策反映在代码结构中
    ✓ 命名约定与 design.md 一致
    ⚠ 设计提到"事件驱动"但实现使用轮询
    
    摘要
    ─────────────────────────────
    关键问题：0
    警告：2
    准备归档：是（有警告）
    
    建议：
    1. 为会话超时场景添加测试
    2. 考虑重构为事件驱动，或更新 design.md
```

---

## 实战案例

### 案例 1：添加深色模式

**场景**：为 Web 应用添加深色主题支持

#### 步骤 1：创建提案

```text
你：/opsx:propose add-dark-mode

AI：已创建 openspec/changes/add-dark-mode/
```

**生成的 proposal.md**：

```markdown
# 提案：添加深色模式

## 意图
用户要求提供深色模式选项，以减少夜间使用时的眼睛疲劳。

## 范围
- 在设置中添加主题切换
- 支持系统偏好检测
- 在 localStorage 中持久化偏好

## 方法
使用 CSS 自定义属性进行主题化，使用 React 上下文进行状态管理。
```

**生成的 specs/ui/spec.md**：

```markdown
# UI 增量规范

## 添加的需求

### 需求：主题选择
系统应允许用户在浅色和深色主题之间切换。

#### 场景：手动切换
- 给定：用户在任何页面
- 当：用户点击主题切换按钮
- 那么：主题立即切换
- 并且：偏好在会话间持久化

#### 场景：系统偏好
- 给定：用户没有保存的偏好
- 当：应用加载
- 那么：使用系统的首选配色方案
```

**生成的 tasks.md**：

```markdown
# 任务

## 1. 主题基础设施
- [ ] 1.1 创建带有浅色/深色状态的 ThemeContext
- [ ] 1.2 为颜色添加 CSS 自定义属性
- [ ] 1.3 实现 localStorage 持久化

## 2. UI 组件
- [ ] 2.1 创建 ThemeToggle 组件
- [ ] 2.2 将切换添加到设置页面
- [ ] 2.3 更新 Header 以包含快速切换

## 3. 样式
- [ ] 3.1 定义深色主题调色板
- [ ] 3.2 更新组件以使用 CSS 变量
```

#### 步骤 2：实现

```text
你：/opsx:apply

AI：正在执行任务...
    ✓ 1.1 创建了带有浅色/深色状态的 ThemeContext
    ✓ 1.2 将 CSS 自定义属性添加到 globals.css
    ✓ 1.3 实现了 localStorage 持久化
    ✓ 2.1 创建了 ThemeToggle 组件
    ✓ 2.2 将切换添加到设置页面
    ✓ 2.3 更新了 Header 以包含快速切换
    ✓ 3.1 定义了深色主题调色板
    ✓ 3.2 更新了组件以使用 CSS 变量
    所有任务完成！
```

#### 步骤 3：归档

```text
你：/opsx:archive

AI：正在归档 add-dark-mode...
    ✓ 将规范合并到 openspec/specs/ui/spec.md
    ✓ 移动到 openspec/changes/archive/2026-05-03-add-dark-mode/
    完成！
```

### 案例 2：修复登录重定向 Bug

**场景**：用户报告登录后重定向到错误页面

#### 步骤 1：探索问题

```text
你：/opsx:explore

AI：你想探索什么？

你：用户登录后被重定向到 404 页面而不是仪表板。

AI：让我调查登录流程...
    [检查认证代码、路由配置、会话处理]
    
    我发现了问题：
    - LoginForm 在成功后重定向到 `/dashboard`
    - 但路由配置中的路径是 `/app/dashboard`
    - 会话正确创建，只是重定向路径不匹配
    
    修复方法：
    1. 更新 LoginForm 重定向到 `/app/dashboard`
    2. 或者添加从 `/dashboard` 到 `/app/dashboard` 的重定向
    
    推荐选项 1（更简单）。
```

#### 步骤 2：创建修复

```text
你：/opsx:propose fix-login-redirect

AI：已创建 openspec/changes/fix-login-redirect/
    ✓ proposal.md
    ✓ specs/auth/spec.md
    ✓ design.md
    ✓ tasks.md
```

**生成的 tasks.md**：

```markdown
# 任务

## 1. 修复重定向
- [ ] 1.1 更新 LoginForm 中的重定向路径
- [ ] 1.2 添加测试以验证正确的重定向
- [ ] 1.3 检查其他可能有相同问题的登录点
```

#### 步骤 3：实现和验证

```text
你：/opsx:apply

AI：正在执行任务...
    ✓ 1.1 将 LoginForm 重定向更新为 `/app/dashboard`
    ✓ 1.2 添加了重定向测试
    ✓ 1.3 检查了 OAuth 回调（也已修复）
    所有任务完成！

你：/opsx:verify

AI：验证 fix-login-redirect...
    ✓ 所有任务完成
    ✓ 测试通过
    ✓ 实现符合规范
    准备归档！

你：/opsx:archive

AI：✓ 已归档 fix-login-redirect
```

### 案例 3：并行开发多个功能

**场景**：同时开发主功能和处理紧急 bug

#### 步骤 1：开始主功能

```text
你：/opsx:propose add-payment-integration

AI：已创建 openspec/changes/add-payment-integration/
    准备好实现了！

你：/opsx:apply

AI：正在执行任务...
    ✓ 1.1 设置 Stripe SDK
    ✓ 1.2 创建支付 API 端点
    [正在进行中...]
```

#### 步骤 2：切换到紧急 bug

```text
你：我需要暂停支付集成，有个紧急 bug 需要修复

你：/opsx:propose fix-email-validation

AI：已创建 openspec/changes/fix-email-validation/

你：/opsx:apply

AI：正在执行 fix-email-validation...
    ✓ 1.1 更新了邮箱正则表达式
    ✓ 1.2 添加了验证测试
    所有任务完成！

你：/opsx:archive

AI：✓ 已归档 fix-email-validation
```

#### 步骤 3：恢复主功能

```text
你：让我们回到支付集成

你：/opsx:apply add-payment-integration

AI：恢复 add-payment-integration...
    从任务 2.1 继续：创建支付表单...
    ✓ 2.1 创建了支付表单组件
    ✓ 2.2 实现了卡片输入验证
    [继续...]
```

#### 步骤 4：批量归档

```text
你：/opsx:bulk-archive

AI：找到 2 个已完成的变更：
    - add-payment-integration（所有任务完成）
    - update-footer（所有任务完成）
    
    检查规范冲突...
    ✓ 没有冲突
    
    归档所有 2 个变更？

你：是

AI：✓ 已归档 add-payment-integration
    ✓ 已归档 update-footer
```

---

## 命令参考

### Core 模式命令

| 命令 | 用途 | 示例 |
|------|------|------|
| `/opsx:propose` | 创建变更和所有规划工件 | `/opsx:propose add-search` |
| `/opsx:explore` | 探索想法和调查问题 | `/opsx:explore` |
| `/opsx:apply` | 实现任务 | `/opsx:apply` |
| `/opsx:sync` | 合并增量规范 | `/opsx:sync` |
| `/opsx:archive` | 完成变更 | `/opsx:archive` |

### Expanded 模式命令

| 命令 | 用途 | 示例 |
|------|------|------|
| `/opsx:new` | 创建变更脚手架 | `/opsx:new add-feature` |
| `/opsx:continue` | 创建下一个工件 | `/opsx:continue` |
| `/opsx:ff` | 创建所有规划工件 | `/opsx:ff` |
| `/opsx:verify` | 验证实现 | `/opsx:verify` |
| `/opsx:bulk-archive` | 归档多个变更 | `/opsx:bulk-archive` |
| `/opsx:onboard` | 项目入职 | `/opsx:onboard` |

### CLI 命令

```bash
# 列出活动变更
openspec list

# 显示变更详情
openspec show <change-name>

# 验证规范格式
openspec validate <change-name>

# 打开交互式仪表板
openspec view

# 配置工作流
openspec config profile

# 更新 AI 指令
openspec update
```

---

## 最佳实践

### 1. 保持变更聚焦

每个变更一个逻辑工作单元。

**好的命名**：
- `add-dark-mode`
- `fix-login-redirect`
- `optimize-product-query`
- `implement-2fa`

**避免的命名**：
- `feature-1`
- `update`
- `changes`
- `wip`

### 2. 使用探索来澄清需求

在创建工件之前探索问题空间：

```text
你：/opsx:explore

AI：你想探索什么？

你：我们应该如何处理 API 的速率限制？

AI：让我调查你当前的设置和选项...
    [研究代码库，考虑模式]
    
    我看到三种方法：
    1. 基于中间件（简单，每个端点）
    2. 基于 Redis（可扩展，共享状态）
    3. 每用户令牌桶（公平，复杂）
    
    你当前的技术栈建议 #1 或 #2。你的规模是多少？
```

### 3. 归档前验证

使用 `/opsx:verify` 检查实现是否符合工件：

```text
你：/opsx:verify

AI：验证 add-dark-mode...
    ✓ tasks.md 中的所有任务已完成
    ✓ 规范中的需求有对应代码
    ✓ 设计决策反映在实现中
    准备归档！
```

### 4. 何时更新 vs 重新开始

**更新现有变更**：
- 相同意图，改进执行
- 范围缩小（先 MVP，其余稍后）
- 学习驱动的修正
- 基于实现发现的设计调整

**开始新变更**：
- 意图根本改变
- 范围扩大到完全不同的工作
- 原始变更可以独立标记为"完成"
- 补丁会造成更多混乱

### 5. 并行工作的策略

处理多个变更时：

1. 使用清晰的命名以便识别
2. 在切换前完成小变更
3. 使用 `/opsx:bulk-archive` 批量归档
4. 保持上下文清晰

---

## 常见问题

### Q1：如何在项目中途采用 OpenSpec？

**答**：运行 `openspec init`，然后使用 `/opsx:onboard` 让 AI 分析现有代码并生成初始规范。

```text
你：/opsx:onboard

AI：分析你的代码库...
    发现以下领域：
    - 认证（登录、注册、会话）
    - 用户管理（个人资料、设置）
    - 产品（列表、详情、搜索）
    
    为这些领域创建初始规范？

你：是

AI：✓ 创建了 openspec/specs/auth/spec.md
    ✓ 创建了 openspec/specs/user/spec.md
    ✓ 创建了 openspec/specs/product/spec.md
```

### Q2：如何处理规范冲突？

**答**：OpenSpec 在归档时自动检测冲突。如果多个变更触及相同规范，它会按时间顺序应用。

```text
你：/opsx:bulk-archive

AI：⚠ add-dark-mode 和 update-footer 都触及 specs/ui/
    
    检查代码库以解决...
    两个变更都已实现。将按时间顺序应用。
    
    归档顺序：add-dark-mode → update-footer
```

### Q3：可以自定义工件模板吗？

**答**：可以。在 `openspec/config.yaml` 中配置自定义模板：

```yaml
templates:
  proposal: .openspec/templates/proposal.md
  design: .openspec/templates/design.md
```

详见[自定义文档](https://github.com/Fission-AI/OpenSpec/blob/main/docs/customization.md)。

### Q4：OpenSpec 支持哪些 AI 工具？

**答**：支持 25+ AI 编码工具，包括：
- Claude Code
- GitHub Copilot
- Cursor
- Windsurf
- Cline
- Aider
- Continue
- 等等

完整列表见[支持的工具](https://github.com/Fission-AI/OpenSpec/blob/main/docs/supported-tools.md)。

### Q5：如何禁用遥测？

**答**：设置环境变量：

```bash
export OPENSPEC_TELEMETRY=0
# 或
export DO_NOT_TRACK=1
```

### Q6：可以在团队中使用 OpenSpec 吗？

**答**：可以。OpenSpec 基于文件，可以通过 Git 共享：

1. 提交 `openspec/` 目录到版本控制
2. 团队成员拉取更新
3. 每个人都能看到活动变更和规范
4. 使用 PR 审查变更

### Q7：如何回滚变更？

**答**：变更归档到 `openspec/changes/archive/`，包含完整历史。要回滚：

1. 找到归档的变更
2. 检查应用了哪些规范更改
3. 手动恢复代码和规范
4. 或创建新的"revert-X"变更

---

## 总结

OpenSpec 通过轻量级规范层改善了 AI 辅助开发：

✅ **在编码前达成一致** - 人类和 AI 对需求对齐  
✅ **保持组织** - 每个变更都有自己的文件夹  
✅ **流畅工作** - 随时更新任何工件  
✅ **使用你的工具** - 通过斜杠命令支持 25+ AI 助手  

**下一步**：

1. 安装 OpenSpec：`npm install -g @fission-ai/openspec@latest`
2. 初始化项目：`openspec init`
3. 创建第一个变更：`/opsx:propose your-feature`
4. 开始构建！

**资源链接**：

- [GitHub 仓库](https://github.com/Fission-AI/OpenSpec)
- [官方文档](https://github.com/Fission-AI/OpenSpec/tree/main/docs)
- [Discord 社区](https://discord.gg/YctCnvvshC)
- [问题反馈](https://github.com/Fission-AI/OpenSpec/issues)

---

**文档版本**：v1.3.1  
**最后更新**：2026-05-03  
**作者**：基于 OpenSpec 官方文档整理
