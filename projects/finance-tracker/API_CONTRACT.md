# API 契约 — 金橘记账

## 认证模块

### POST /api/auth/register
```json
// Request:  { "email": "demo@jijin.com", "password": "123456" }
// Response: { "access_token": "...", "refresh_token": "..." }
```

### POST /api/auth/login
```json
// Request:  { "email": "demo@jijin.com", "password": "123456" }
// Response: { "access_token": "...", "refresh_token": "..." }
```

### POST /api/auth/refresh
```json
// Request:  ?refresh_token=...
// Response: { "access_token": "...", "refresh_token": "..." }
```

### GET /api/auth/me
```json
// Response: { "id": "uuid", "email": "demo@jijin.com", "nickname": "阿珊", "created_at": "..." }
```

---

## 账单模块

### GET /api/transactions?month=2026-06&category=餐饮&type=expense&page=1&page_size=20
```json
// Response: {
//   "items": [{
//     "id": "uuid",
//     "type": "expense" | "income",
//     "amount": 3200,        // 单位：分
//     "category": "餐饮",
//     "category_emoji": "🍽️",
//     "note": "午餐",
//     "date": "2026-06-29",
//     "created_at": "2026-06-29T12:30:00"
//   }],
//   "total": 42,
//   "summary": { "income": 1280000, "expense": 437950 }
// }
```

### POST /api/transactions
```json
// Request: {
//   "type": "expense",
//   "amount": 3200,
//   "category": "餐饮",
//   "note": "午餐 - 黄焖鸡",
//   "date": "2026-06-29"
// }
// Response: { "id": "uuid", ... }
```

### PUT /api/transactions/:id
```json
// Request:  { "note": "午餐 - 黄焖鸡米饭", "amount": 3500 }
// Response: { "id": "uuid", ... }
```

### DELETE /api/transactions/:id
```json
// Response: { "ok": true }
```

---

## 预算模块

### GET /api/budgets?month=2026-06
```json
// Response: {
//   "total_budget": 800000,
//   "total_spent": 416000,
//   "categories": [
//     { "category": "餐饮", "emoji": "🍽️", "budget": 200000, "spent": 120000 },
//     ...
//   ]
// }
```

### PUT /api/budgets
```json
// Request: {
//   "month": "2026-06",
//   "total_budget": 800000,
//   "categories": [
//     { "category": "餐饮", "budget": 200000 },
//     ...
//   ]
// }
```

---

## 仪表盘模块

### GET /api/dashboard?month=2026-06
```json
// Response: {
//   "balance": 842050,
//   "month_income": 1280000,
//   "month_expense": 437950,
//   "recent_transactions": [ ... ],   // 最近 5 条
//   "budget_usage": 52               // 预算使用百分比
// }
```

---

## 分类模块

### GET /api/categories
```json
// Response: {
//   "expense": [
//     { "name": "餐饮", "emoji": "🍽️" },
//     { "name": "交通", "emoji": "🚇" },
//     ...
//   ],
//   "income": [
//     { "name": "工资", "emoji": "💰" },
//     ...
//   ]
// }
```

---

## 用户模块

### GET /api/user/profile
```json
// Response: { "id": "uuid", "email": "...", "nickname": "阿珊", "avatar": "😊", "joined_date": "2026-03-01", "total_days": 128 }
```

### PUT /api/user/profile
```json
// Request:  { "nickname": "阿珊", "avatar": "😊" }
// Response: { "id": "uuid", ... }
```

### GET /api/user/stats
```json
// Response: {
//   "total_count": 1286,
//   "total_income": 5280000,
//   "total_expense": 3842000,
//   "current_streak": 12  // 连续记账天数
// }
```

---

## 统计模块

### GET /api/stats/trend?month=2026-06
```json
// Response: {
//   "daily": [{ "date": "2026-06-23", "amount": 4500 }, ...],  // 最近7天
//   "daily_avg": 14600,
//   "vs_last_week": -12  // 较上周百分比
// }
```

### GET /api/stats/category?month=2026-06&type=expense
```json
// Response: {
//   "categories": [
//     { "name": "餐饮", "emoji": "🍽️", "amount": 148000, "percentage": 34 },
//     ...
//   ]
// }
```
