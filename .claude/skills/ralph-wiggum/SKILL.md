---
name: ralph-wiggum
description: Autonomous iterative development loop. Feed a prompt, let Claude work through it in repeated cycles with clean context each iteration, self-correcting via file system and git history. Inspired by Geoffrey Huntley's Ralph Wiggum technique.
---

# Ralph Wiggum · 自主迭代开发循环

Named after the Simpsons character who never gives up — persistence beats perfection.

## 核心理念

每次迭代用全新上下文运行同一个 prompt，AI 通过文件系统和 git 历史看到之前的工作，实现自我修正和渐进式完善。

```
while :; do
  cat PROMPT.md | claude
done
```

## 使用方式

**启动循环：**
```
/ralph-loop "<你的任务描述>" --max-iterations 10
```

**取消循环：**
```
/cancel-ralph
```

## 关键原则

1. **迭代 > 完美** — 不要追求第一次就完美，让 AI 在循环中自我改进
2. **"坐在循环上，不要坐在循环里"** — 监控进度而非微观管理
3. **失败是可预测的** — 每次失败都是调优的机会
4. **干净上下文 = 低成本** — 每轮都是全新对话，token 可控

## 适用场景

- 从零搭建完整项目骨架
- 批量修复 lint 错误
- 大规模代码迁移
- 自动生成测试用例
- 多文件重构

## 参考

- 原创作者: Geoffrey Huntley
- Awesome Ralph 资源列表: https://github.com/snwfdhmp/awesome-ralph
- 官方 Claude Code 插件: anthropics/claude-code/plugins/ralph-wiggum
