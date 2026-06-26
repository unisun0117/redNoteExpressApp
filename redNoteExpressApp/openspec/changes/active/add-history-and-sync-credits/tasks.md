# 任务清单

## 1. 后端：新增历史记录接口

- [ ] 1.1 在 `routers/generate.py` 中新增 `GET /api/generations` 路由
- [ ] 1.2 查询当前用户的 Generation 记录，按时间倒序，支持分页（skip/limit）
- [ ] 1.3 返回 JSON：items 数组 + total 总数

## 2. 前端：历史文章列表

- [ ] 2.1 在 DashboardPage 新增"历史文章"tab
- [ ] 2.2 调用 `/api/generations` 获取列表
- [ ] 2.3 渲染列表：标题 + 关键词 + 时间
- [ ] 2.4 点击展开显示完整文章（复用 ResultView）
- [ ] 2.5 每条记录支持一键复制

## 3. 前端：点数动态同步

- [ ] 3.1 在 `useAuth.tsx` 中新增 `refreshUser()` 方法
- [ ] 3.2 在 GeneratorPage 生成成功后调用 `refreshUser()`
- [ ] 3.3 验证右上角点数实时更新

## 4. 课程同步更新

- [ ] 4.1 更新 Lab07（生成页面）：补充点数刷新逻辑
- [ ] 4.2 更新 Lab08（结果展示）：提及历史列表功能
- [ ] 4.3 更新 Lab10（点数计费）：补充动态刷新说明
- [ ] 4.4 更新 Lab13（坑点大全）：新增相关坑点
