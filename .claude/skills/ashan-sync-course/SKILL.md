---
name: ashan-sync-course
description: Use when new features are added to the project via openspec or any code changes that affect existing course Labs. Triggers on phrases like "同步课程", "更新课程Lab", "项目改完了更新课程", "新功能加到课程里".
---

# 阿珊-同步课程内容

## Overview

当项目代码有改动（新增功能、修 bug、重构）时，自动检查哪些课程 Lab 受影响，并更新对应的课程文档。确保课程内容始终和项目源码保持一致。

## 工作流程

```
项目代码变更
    ↓
1. 识别影响范围（哪些文件改了？）
    ↓
2. 映射到课程 Lab（改了 generate.py → Lab04/Lab06/Lab07/Lab10 需要更新）
    ↓
3. 读取受影响的 Lab .md 文件
    ↓
4. 更新 Lab 内容（补充新代码、新坑点、新流程）
    ↓
5. 保存更新后的 Lab 文件
```

## 影响映射表

根据改动的文件，自动匹配受影响的课程 Lab：

| 改动的文件 | 受影响的 Lab |
|-----------|-------------|
| `backend/app/main.py` | Lab02, Lab12 |
| `backend/app/config.py` | Lab03, Lab04 |
| `backend/app/database.py` | Lab03 |
| `backend/app/models/*.py` | Lab03 |
| `backend/app/routers/auth.py` | Lab04, Lab05 |
| `backend/app/routers/generate.py` | Lab06, Lab07, Lab10 |
| `backend/app/routers/styles.py` | Lab09 |
| `backend/app/routers/billing.py` | Lab10 |
| `backend/app/services/generator.py` | Lab06, Lab09 |
| `backend/app/middleware/auth.py` | Lab04, Lab05 |
| `frontend/src/pages/GeneratorPage.tsx` | Lab07, Lab10 |
| `frontend/src/pages/DashboardPage.tsx` | Lab05, Lab10 |
| `frontend/src/pages/LoginPage.tsx` | Lab05 |
| `frontend/src/services/api.ts` | Lab05, Lab07 |
| `frontend/src/hooks/useAuth.tsx` | Lab05 |
| `frontend/src/components/*.tsx` | Lab07, Lab08, Lab09 |
| `openspec/` 新增变更 | Lab13（坑点）, Lab14（知识储备） |
| `docs/courses/` 已有课程 | Lab15（总结复盘） |

## 更新课程 Lab 的规则

### 1. 代码变了 → 更新代码示例

如果 Lab 中的代码片段和项目实际代码不一致，用实际代码替换。

### 2. 新增功能 → 补充章节

如果项目新增了功能，在相关 Lab 末尾补充"补充：新功能说明"。

### 3. 新增坑点 → 更新坑点表

如果开发和测试过程中遇到新坑，加到对应 Lab 的"常见坑点"表格。

### 4. 保持阿珊风格

更新内容必须保持：
- 前言有个人故事
- 金句密度不降低
- 署名不变

## 快速参考

| 用户说 | 做什么 |
|--------|--------|
| "同步课程" | 检查最近的代码改动，更新受影响的 Lab |
| "我改了 generate.py，更新课程" | 更新 Lab04, Lab06, Lab07, Lab10 |
| "新增了历史列表功能，同步到课程" | 更新相关 Lab + Lab13 + Lab14 + Lab15 |
