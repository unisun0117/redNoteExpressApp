---
name: product-spec-engineer
description: 产品SPEC文档工程与质量检查技能 - 用于分析、生成、评估和实现产品规格文档，确保功能定义转化为结构化、无歧义的可实现蓝图
version: 1.0.0
author: Product Engineering Team
triggers:
  - 生成SPEC
  - 产品SPEC
  - 产品需求文档
  - 需求文档
  - PRD文档
  - 功能规范
  - 数据字典
  - 界面字段
  - 业务流程
  - 产品设计
  - 需求分析
  - 功能定义
categories:
  - product-management
  - documentation
  - quality-gate
  - specification
dependencies: []
tools: []
---

# Skill: Product SPEC Engineering & Quality Gate

## 1. Description & Objective
This skill defines the operational standards and quality gates for analyzing, generating, evaluating, and implementing Product Specification (SPEC) documents. It ensures that any feature definition or development requirement parsed by the AI Swarm translates into structured, unambiguous, and production-ready implementation blueprints, eliminating technical debt and alignment drift between Product Managers and Engineering teams.

---

## 2. Activation Triggers
Activate this skill immediately when the user or any agent initiates tasks involving:
- Creating or editing Product Requirements Documents (PRDs) or feature SPECs.
- Analyzing new feature requests, functional modules, or business logic.
- Refining system boundaries, UI/UX field mappings, or database-to-field synchronization rules.
- Directing the `architect`, `planner`, or `coder` agents during a `Feature` or `Refactor` workflow.

---

## 3. Core Execution Rules & Constraints

### General Guardrails
- **Scope Compliance**: Do exactly what has been asked; nothing more, nothing less.
- **File Management**: NEVER create new files unless absolutely necessary — prefer editing existing files. NEVER create separate documentation files unless explicitly requested; embed specifications natively or follow strict path guidelines.
- **Path Isolation**: NEVER save temporary working files or test artifacts to the project root. Use designated structures: `/src`, `/tests`, `/docs`, `/config`, `/scripts`.
- **Pre-read Enforcement**: ALWAYS read a target file entirely before executing any edit.
- **Security & Integrity**: NEVER commit secrets, credentials, API keys, or `.env` files. Validate all inputs tightly at system boundaries.
- **Image Recognition**: 当用户提交图片（截图、设计稿、原型图等）需要识别其中内容时，调用 `product/gemini.py` 进行分析：
  ```bash
  python product/gemini.py "<image_path>"
  ```
  可选自定义提示词：`python product/gemini.py "<image_path>" --prompt "自定义分析指令"`
- **File Metrics**: Keep generated or refactored files under 500 lines to ensure high cohesion and maintainability.
- **Git Attribution**: NEVER add a `Co-Authored-By` trailer to user commits unless `.claude/settings.json` has `attribution.commit` set to true.

---

## 4. Product SPEC Standard (The 7-Tier Framework)

When executing this skill to output or evaluate product requirements, the response MUST systematically fulfill the following 7 dimensions with maximum precision and zero conversational fluff:

### 4.1 背景与目标 (Background & Objectives)
- **一句话背景**：Clear, concise statement answering *why* this feature is being built (e.g., "Resolve the inability for users to self-modify registered mobile numbers"). If there is no explicit background provided, state "无".

### 4.2 角色与使用场景 (Roles & Scenarios)
- **目标用户**：Explicitly define the interacting personas (e.g., B-End Client, Store Manager, Warehouse operator, Super Admin).
- **使用场景**：Describe the context, environment, and user objective utilizing standard User Story formatting (*"As a [role], I want to [action] so that [benefit]"*).

### 4.3 核心业务流程 (Core Business Flow)
- **主干流程**：The primary positive, happy-path sequence from initiation to completion. Represent using sequential step lists or standard Mermaid sequence/flowcharts.
- **状态流转**：Define all states of the document, transaction, or entity along with their exact state transition triggers.
- **异常流/逆向流**：Mandatorily detail system behavior and user notifications when preconditions fail or infrastructure exceptions occur (e.g., inventory shortage, network timeout, workflow rejection).

### 4.4 界面与交互说明 (UI & Interaction)

- **界面布局**：Use ASCII diagrams to describe the anatomical structure of the page (such as search area, data table, detail drawer, centered popup), labeling the functional positioning of each area.
- **交互动作**：Exact tactical response following a user interaction (e.g., hover states, button clicks, swipe gestures, drag-and-drop actions).
- **极限状态**：Define handling for extreme conditions: Empty State, Loading/Skeleton state, and high-volume data rendering (Pagination, Infinite Scroll, or Virtual List).

#### ASCII 布局格式规范
- Use `┌ ┐ └ ┘ ├ ┤ ─ │ ┬ ┴ ┼` and other box line characters to draw borders
- Mark the component type and keyword segment name in the area, use `[button text]` to represent the button, `[field name ▼]` to represent the drop-down, and `[placeholder ____]` to represent the input box.
- The data table needs to list all column names and give at least one line of sample data.

**后台示例**：
```
┌─────────────────────────────────────────────────────────────────┐
│  搜索区                                                          │
│  客户信息：[客户编号/名称____]  营业执照编号：[________]  账户类型：[全部 ▼]  [重置] [查询] │
├─────────────────────────────────────────────────────────────────┤
│  [新增]                                               共 X 条记录│
├─────────────────────────────────────────────────────────────────┤
│  数据表格（11列）                                                 │
│  ┌────┬──────┬──────┬──────┬──────┬────┬──────┬────┬────┬───┐  │
│  │序号│客户  │客户  │账户  │账户  │账期│下一  │备注│营业│...│  │
│  │    │编号  │名称  │金额  │类型  │天数│对账日│    │执照│   │  │
│  ├────┼──────┼──────┼──────┼──────┼────┼──────┼────┼────┼───┤  │
│  │ 1  │C001  │张三  │1,280 │预付款│ -  │  -   │ -  │... │   │  │
│  └────┴──────┴──────┴──────┴──────┴────┴──────┴────┴────┴───┘  │
├─────────────────────────────────────────────────────────────────┤
│  分页器：[< 上一页] [1] [2] [3] [下一页 >]  共 3 页              │
└─────────────────────────────────────────────────────────────────┘
```
**移动端示例**：
```
┌─────────────────────────────────┐
│  ← 我的钱包                      │
├─────────────────────────────────┤
│  预付款账户：王博A               │
│                                 │
│  可提现余额  ¥ 1,280.00          │
│  冻结金额     ¥ 200.00           │
│                                 │
│  ┌──────────┐ ┌──────────┐     │
│  │   充值   │ │   提现   │     │
│  └──────────┘ └──────────┘     │
│                                 │
│  ┌──────────────────────────────┐ │
│  │         查看交易流水           │ │
│  └──────────────────────────────┘ │
│  ┌──────────────────────────────┐ │
│  │         查看充值提现记录        │ │
│  └──────────────────────────────┘ │
└─────────────────────────────────┘
```


### 4.5 数据字典与字段级规则 (Data & Field Rules)
All input forms, data listings, and filters must be mapped out using a structural field dictionary matrix:

| 字段名称 | 字段类型 | 来源/依赖 | 默认值 | 读写权限 | 校验规则与约束 | 说明/占位符 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| (e.g., 订货量) | Integer | User Input / Model Output | 0 | Editable | >0, <= Max Stock Threshold, Required | Helper tooltip text |

- **展示逻辑**：Strict formatting requirements (e.g., Date strings format as `YYYY-MM-DD HH:mm`, currencies rounded to two decimal places).
- **编辑逻辑**：Contextual mutability rules detailing exactly *when* and *under what status* a specific field converts to read-only, hidden, or editable.

### 4.6 系统交互与边界 (System Integrations & Boundaries)
- **前置依赖**：Technical or operational prerequisites that must be satisfied before execution (e.g., "Must complete CAS authentication, verify TGT, and fetch ST prior to data callback routing").
- **上下游影响**：Downstream data sync or ledger mutations triggered by this action (e.g., billing engine records, inventory ledger adjustments).

### 4.7 非功能性需求 (Non-Functional Requirements)
- **性能要求**：Explicit engineering scale indicators (Concurrency limits, maximum acceptable latency, bulk processing batch sizes).
- **权限与安全**：Enforce granular security controls covering Data-Level access (e.g., "Can only query local department records") and Button-Level operation privileges.

---

## 5. Swarm Routing & Multi-Agent Orchestration

When a feature implementation requires cross-module modification or algorithmic integration, the Lead agent coordinates via a **Hierarchical-Mesh Topology** utilizing a max budget of 15 specialized agents.

### Team Pipeline Initialization
```javascript
// Spawn the entire cross-functional team in a single non-blocking cycle
Agent({ prompt: "Research the codebase context for the SPEC. SendMessage findings to 'architect'.",
  subagent_type: "researcher", name: "researcher", run_in_background: true })
Agent({ prompt: "Wait for 'researcher'. Design technical architecture based on the 7-Tier SPEC. SendMessage to 'coder'.",
  subagent_type: "system-architect", name: "architect", run_in_background: true })
Agent({ prompt: "Wait for 'architect'. Implement core logic and field rules. SendMessage to 'tester'.",
  subagent_type: "coder", name: "coder", run_in_background: true })
Agent({ prompt: "Wait for 'tester'. Write unit and integration tests covering the exception flows. SendMessage to 'reviewer'.",
  subagent_type: "tester", name: "tester", run_in_background: true })
Agent({ prompt: "Wait for 'tester'. Review security metrics, permission layers, and quality bounds.",
  subagent_type: "reviewer", name: "reviewer", run_in_background: true })

// Execute pipeline entry
SendMessage({ to: "researcher", summary: "Start Feature Execution", message: "[PRODUCT/PRD/SPEC Context Data]" })