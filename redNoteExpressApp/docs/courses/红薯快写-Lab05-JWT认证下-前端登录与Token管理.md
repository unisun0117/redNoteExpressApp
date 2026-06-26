# Lab 05：JWT 用户认证（下）— 前端登录页 + Token 管理

> **预计用时：** 40-45 分钟
> **难度：** ⭐⭐
> **前置 Lab：** Lab 04（后端注册登录接口必须能调通）

---

## 📌 前言

后端接口写好了，但用户不能每次都去 Swagger 页面登录吧？这一 Lab 我们要做的是：一个正儿八经的前端登录页面，输入邮箱密码，点登录，跳转到生成页。

听起来简单，但有三个细节容易被忽略：**token 存哪里？过期了怎么办？怎么防止未登录用户直接访问生成页？** 这三个问题搞不定，登录就是个半成品。这 Lab 我把整套逻辑拆开给你看——从 `api.ts` 的自动刷新机制到 `useAuth.tsx` 的全局状态管理。

---

## 📚 基础知识储备

- **localStorage** — 浏览器本地存储，数据关了浏览器还在。我们用它存 token
- **React Context** — 全局状态管理，让所有组件都能读到用户信息（不用一层层 props 传）
- **路由守卫（Private Route）** — 未登录用户访问需要登录的页面时，自动跳转到登录页
- **fetch API** — 浏览器原生 HTTP 请求方法

---

## 🎯 实战目标

学完这个 Lab，你将能够：

1. 实现前端登录页面的完整交互
2. 理解 token 的存储和自动刷新机制
3. 实现路由守卫——未登录自动跳转登录页
4. 在页面上显示用户剩余点数

---

## 🛠 动手实战

### 步骤 1：读懂 API 封装层 `services/api.ts`

这是整个前端认证体系的地基。

**打开文件：** `frontend/src/services/api.ts`

```typescript
const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8000/api";

// token 存在模块变量里（快），同时存 localStorage（持久化）
let accessToken: string | null = localStorage.getItem("access_token");
let refreshToken: string | null = localStorage.getItem("refresh_token");

// 存 token
function setTokens(access: string, refresh: string) {
  accessToken = access;
  refreshToken = refresh;
  localStorage.setItem("access_token", access);
  localStorage.setItem("refresh_token", refresh);
}

// 清除 token（退出登录）
function clearTokens() {
  accessToken = null;
  refreshToken = null;
  localStorage.removeItem("access_token");
  localStorage.removeItem("refresh_token");
}
```

---

### 步骤 2：理解核心——自动刷新 token 的 `authFetch`

```typescript
async function authFetch(path: string, options: RequestInit = {}): Promise<Response> {
  const headers: Record<string, string> = {
    ...(options.headers as Record<string, string>),
  };
  // 自动带上 Bearer token
  if (accessToken) {
    headers["Authorization"] = `Bearer ${accessToken}`;
  }

  let res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  // 🔑 关键逻辑：如果返回 401，尝试用 refresh token 刷新
  if (res.status === 401 && refreshToken) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      // 刷新成功，用新 token 重试请求
      headers["Authorization"] = `Bearer ${accessToken}`;
      res = await fetch(`${BASE_URL}${path}`, { ...options, headers });
    } else {
      // 刷新失败，清掉 token 跳转登录页
      clearTokens();
      window.location.href = "/login";
    }
  }
  return res;
}
```

**这个函数做的事：**
1. 每次请求自动带 `Authorization: Bearer <token>` 头
2. 如果后端返回 401（token 过期），自动用 refresh token 换新的
3. 如果刷新也失败，说明 refresh token 也过期了 → 清空 token → 跳登录页

**这个设计好在哪？** 用户完全无感——token 过期了自动刷新，不需要用户重新登录。

---

### 步骤 3：理解全局认证状态 `hooks/useAuth.tsx`

```typescript
import { useState, useEffect, createContext, useContext, type ReactNode } from "react";
import { api, setTokens, clearTokens, accessToken } from "../services/api";

interface User {
  id: string;
  email: string;
  tier: string;
  credits_remaining: number;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType>(null!);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  // 页面加载时：如果 localStorage 里有 token，自动获取用户信息
  useEffect(() => {
    if (accessToken) {
      api.getMe().then((data) => {
        if (data.id) setUser(data);
        setLoading(false);
      }).catch(() => {
        clearTokens();
        setLoading(false);
      });
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (email: string, password: string) => {
    const data = await api.login(email, password);
    setTokens(data.access_token, data.refresh_token);
    const me = await api.getMe();
    setUser(me);
  };

  const logout = () => {
    clearTokens();
    setUser(null);
  };
  // ...
}
```

**流程：** 页面加载 → 检查 localStorage 有没有 token → 有就调 `/api/auth/me` 获取用户信息 → 存入全局状态 → 所有组件都能用 `useAuth()` 拿到用户信息。

---

### 步骤 4：理解路由守卫 `App.tsx`

```typescript
function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  if (loading) return <div>Loading...</div>;
  if (!user) return <Navigate to="/login" />;  // ← 没登录？跳走
  return <>{children}</>;
}
```

任何需要登录才能看的页面，包一层 `<PrivateRoute>` 就搞定。

---

### 步骤 5：启动前端，跑通完整登录流程

**操作：**
```bash
# 终端 1：启动后端
cd E:\AI_Workspace\redNoteExpressApp\backend
venv\Scripts\activate
.\venv\Scripts\uvicorn.exe app.main:app --reload --port 8000

# 终端 2：启动前端
cd E:\AI_Workspace\redNoteExpressApp\frontend
npm run dev
```

1. 浏览器打开 http://localhost:5173
2. 应该自动跳转到 `/login`（因为还没登录）
3. 点"注册"标签，输入邮箱密码注册
4. 注册成功 → 自动跳转到生成页
5. 刷新页面 → 仍然在生成页（token 存在 localStorage 里）
6. 生成页右上角能看到"剩余：10 次"
7. 点"我的" → 能看到用户信息

**验证：** 完整登录流程跑通 ✅

---

## ⚠️ 常见坑点

| 坑 | 现象 | 原因 | 解决 |
|----|------|------|------|
| VITE_API_URL 没配 | 前端请求发到了 localhost:8000 但后端在别的端口 | 没配环境变量 | 在 `frontend/.env` 里写 `VITE_API_URL=http://localhost:8000/api` |
| CORS 报错 | 浏览器 Console 里看到 CORS 错误 | 后端没配好 ALLOWED_ORIGINS | 检查 `config.py` 里的 `ALLOWED_ORIGINS` 是否包含前端地址 |
| 登录后刷新页面又要重新登录 | localStorage 丢了 | token 没正确存储 | 检查 `setTokens` 是否调用了 |
| 401 后没自动刷新 | 页面直接跳到 /login | `refreshAccessToken` 逻辑没触发 | 确认后端 refresh 接口正常 |

---

## 📝 总结

**本章核心要点：**
- `api.ts` 的 `authFetch` 是精髓——自动带 token + 自动刷新 + 失败跳登录
- `useAuth` 提供全局认证状态，任何组件用 `useAuth()` 就能拿到 user
- `PrivateRoute` 守卫需要登录的页面
- Token 存在 localStorage 里，关了浏览器再打开还在

**你现在应该能做到：**
- 说出从前端登录到拿到 token 再到后续请求带 token 的完整链路
- 理解 token 自动刷新的原理
- 独立实现一个带登录功能的前端页面

**下一步：** Lab 06 我们接入 AI 接口——让 DeepSeek 真正生成小红书文案！

---

> —— 阿珊，前端开发者 & AI 提效实践者
