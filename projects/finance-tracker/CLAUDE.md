# 金橘记账 — 全栈项目

## 启动命令

```bash
# 后端
cd E:\AI_Workspace\projects\finance-tracker\backend
.\venv\Scripts\uvicorn.exe app.main:app --reload --port 8001

# 前端
cd E:\AI_Workspace\projects\finance-tracker\frontend
npm install && npm run dev
```

## 技术栈
- 前端：React 19 + Vite + antd-mobile
- 后端：FastAPI + SQLAlchemy + SQLite
- 认证：JWT (email + password)

## 项目结构
```
backend/app/
├── main.py          # FastAPI 入口
├── config.py        # 配置
├── database.py      # SQLAlchemy
├── models/          # User, Transaction, Budget
├── routers/         # auth, transactions, budgets, stats, profile
└── middleware/      # JWT 认证

frontend/src/
├── App.tsx          # 路由
├── services/api.ts  # API 封装
├── hooks/useAuth.tsx # 认证状态
├── pages/           # 6个页面
└── components/      # TabBar
```

## 金额单位
数据库存储单位为分（int），前端显示为元（/100）
