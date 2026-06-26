# 设计文档：历史文章列表 + 点数动态同步

## 后端设计

### 新增接口：`GET /api/generations`

```
GET /api/generations?skip=0&limit=20
Authorization: Bearer <token>

Response:
{
  "items": [
    {
      "id": "uuid",
      "keywords": "咖啡 下午茶",
      "style_config": {"template": "简约风", "track": "美食", ...},
      "generated_article": {
        "title": "...",
        "intro": "...",
        "sections": [...],
        "summary": "...",
        "store_info": "..."
      },
      "tokens_used": 586,
      "created_at": "2026-06-26T06:39:17"
    }
  ],
  "total": 42
}
```

**实现：** 在 `routers/generate.py` 中新增一个 GET 路由，查询 `Generation` 表，按 `created_at` 倒序，支持分页。

## 前端设计

### 历史文章列表（DashboardPage）

在 DashboardPage 中新增"历史文章"区域：
- 首次加载获取最近 20 条
- 列表项显示：标题 + 关键词标签 + 生成时间
- 点击展开显示完整文章（复用 ResultView 组件）
- 每条记录有"复制"按钮

### 点数动态刷新（GeneratorPage）

在 `GeneratorPage.tsx` 的 `handleGenerate` 中：
- 生成成功后，调用 `useAuth` 提供的刷新方法
- 或者直接在 api.ts 中新增 `refreshUser` 方法

**最简单方案：** 生成成功后调用 `api.getMe()` 更新 user 状态。需要在 `useAuth` 中暴露一个 `refreshUser` 方法。

## 数据流

```
生成成功 → 扣点数 → 保存generation记录 → 返回文章
    ↓
前端收到结果 → 展示文章 → 调 refreshUser() → 右上角点数更新
    ↓
用户去"我的"页面 → GET /api/generations → 渲染历史列表
```
