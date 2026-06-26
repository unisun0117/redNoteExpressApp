# Lab 14：知识储备大全 — 技术深挖 + 面试题

> **预计用时：** 30-40 分钟（随时查阅）
> **难度：** ⭐⭐⭐
> **前置 Lab：** Lab 01-12 全覆盖

---

## 📌 前言

做完项目只是第一步。如果你要面试，面试官会问一些你在项目里"用到了但未必理解透"的技术点——"JWT 的原理是什么？""为什么用 SQLAlchemy ORM 而不是手写 SQL？""FastAPI 和 Flask 有什么区别？"

这个 Lab 把你做项目过程中涉及的核心技术点深挖一遍，加上 20 道常见面试题。面试前翻一遍，你能跟面试官聊清楚每个技术选型背后的"为什么"。

---

## 📚 技术深挖

### 1. FastAPI 为什么快？

FastAPI 的名字里就有"Fast"——但它快的不是执行速度（虽然确实不慢），而是开发速度。

| 对比维度 | FastAPI | Flask | Django |
|----------|---------|-------|--------|
| 异步支持 | ✅ 原生 async/await | ❌ 需要扩展 | ⚠️ 3.1+ 支持 |
| 自动 API 文档 | ✅ Swagger + ReDoc | ❌ 需要插件 | ⚠️ 需要 DRF |
| 数据校验 | ✅ Pydantic 自动校验 | ❌ 手动校验 | ⚠️ Serializer |
| 类型提示 | ✅ Python type hints | ❌ 不支持 | ❌ 不支持 |
| 学习曲线 | 低 | 低 | 高 |

**核心理念：** FastAPI 用 Python 的类型提示（type hints）自动做参数校验、序列化、生成文档。你写一次类型定义，它帮你干三件事。

---

### 2. JWT 认证原理

JWT（JSON Web Token）= 三段 Base64 编码的 JSON，用 `.` 连接：

```
eyJhbGciOi... (Header: 算法 + 类型)
.
eyJzdWIiOi... (Payload: 用户ID + 过期时间)
.
签名 (Header + Payload + Secret → HMAC)
```

**关键特性：**
- **无状态：** 服务器不需要存 Session，token 本身包含所有信息
- **防篡改：** 改了 Payload 会导致签名不匹配，服务器会拒绝
- **不能存敏感信息：** Payload 只是 Base64 编码，不是加密！任何人都能解码看内容
- **Access + Refresh 双 Token：** Access 短期（防泄露），Refresh 长期（用户体验）

---

### 3. SQLAlchemy ORM 为什么不用手写 SQL

```python
# 手写 SQL（容易出错）
cursor.execute("INSERT INTO users (email, password_hash) VALUES (?, ?)", [email, hash])

# SQLAlchemy ORM（类型安全）
user = User(email=email, password_hash=hash)
db.add(user)
db.commit()
```

**ORM 的优势：**
- **防 SQL 注入：** 参数自动转义
- **类型安全：** IDE 能提示 User 有哪些字段
- **数据库无关：** 换数据库（SQLite → PostgreSQL）只改连接字符串，代码不用动
- **可读性：** Python 代码比 SQL 字符串好维护

**手写 SQL 的优势：**
- 复杂查询性能更高
- 对数据库有完全控制权

---

### 4. Prompt Engineering 精髓

这个项目里 AI 文章质量的秘密，全在 Prompt 设计上：

| 层次 | 作用 | 示例 |
|------|------|------|
| **人设（Track）** | 告诉 AI 它是谁 | "你是一个美食探店博主" |
| **风格（Style）** | 告诉 AI 怎么写 | "使用复古怀旧的语气" |
| **格式约束** | 告诉 AI 输出什么 | "严格按 JSON 格式返回" |
| **字数约束** | 控制篇幅 | "标题 18-20 字，正文每段约 100 字" |

**黄金原则：** 指令越具体，输出越可控。把 AI 当实习生——不说清楚，它就自由发挥。

---

### 5. RESTful API 设计规范

| HTTP 方法 | 语义 | 本项目示例 |
|-----------|------|-----------|
| `GET` | 读取 | `GET /api/styles` |
| `POST` | 创建 | `POST /api/auth/register` |
| `PUT` | 完整更新 | （本项目未使用） |
| `PATCH` | 部分更新 | （本项目未使用） |
| `DELETE` | 删除 | （本项目未使用） |

**本项目的前缀规范：**
- `/api/auth/*` — 认证相关
- `/api/generate` — 核心业务
- `/api/styles` — 配置数据
- `/api/billing/*` — 付费相关
- `/api/batch/*` — 批量操作

---

## 📚 常见面试题 20 道

### 项目类
1. **Q: 介绍你做的这个项目**
   A: 红薯快写是一个 AI 驱动的小红书文案生成器。用户上传图片 + 输入关键词 + 选择风格，DeepSeek AI 自动生成一篇完整文章。技术栈 React + FastAPI + SQLite，部署在 Vercel + PythonAnywhere。

2. **Q: 为什么选择这个技术栈？**
   A: React 是我最熟的前端框架；FastAPI 自动生成文档，调试效率高；SQLite 零配置，个人项目够用；DeepSeek 比 OpenAI 便宜很多而且中文效果好。

3. **Q: 最难的模块是哪个？**
   A: AI 生成模块。Prompt 工程调了很多版——字数控制、JSON 输出稳定性、emoji 自然插入，每项都需要反复测试。

### 前端类
4. **Q: React Hooks 用过哪些？**
   A: useState、useEffect、useContext（认证全局状态）、useRef、自定义 Hook（useAuth）。

5. **Q: 怎么管理全局状态的？**
   A: React Context + useAuth Hook。认证信息（用户、token）存在 Context 里，所有组件通过 useAuth() 访问。

6. **Q: 怎么处理 Token 过期的？**
   A: aauthFetch 封装函数——请求返回 401 时自动用 Refresh Token 刷新，失败则跳登录页。用户全程无感。

7. **Q: Vite 和 Webpack 的区别？**
   A: Vite 利用浏览器原生 ESM，开发时不需要打包，启动快（冷启动 <1s）。生产构建用 Rollup。

### 后端类
8. **Q: JWT 的工作原理？**
   A: 三段式结构（Header.Payload.Signature）。服务器用密钥签名，客户端存 token，每次请求带在 Authorization 头里。无状态，防篡改。

9. **Q: 密码怎么存的？为什么不能存明文？**
   A: 用 bcrypt 哈希存储。即使数据库泄露，黑客无法还原原始密码。登录时用哈希比对，不是解密。

10. **Q: 为什么用 SQLite 而不是 MySQL/PostgreSQL？**
    A: SQLite 零配置、零运维，适合个人项目和中小规模应用。如果以后并发高了，SQLAlchemy 换 PostgreSQL 只改一个连接字符串。

11. **Q: 怎么防 SQL 注入的？**
    A: 用 ORM（SQLAlchemy），所有参数自动参数化查询。`db.query(User).filter(User.email == email)` 里 email 会被安全转义。

12. **Q: CORS 是什么？怎么处理的？**
    A: 跨域资源共享。前端 localhost:5173 请求后端 localhost:8000 算跨域。后端通过 CORSMiddleware 设置允许的来源。

### AI 类
13. **Q: 怎么对接 DeepSeek API 的？**
    A: DeepSeek 兼容 OpenAI 接口格式。用 OpenAI SDK，base_url 指向 DeepSeek。调用流程：构建 Prompt → 传图片和文本 → 解析 JSON 响应。

14. **Q: Prompt 是怎么设计的？**
    A: 四层结构——人设（美食博主）、风格（复古/幽默等）、格式约束（JSON）、字数控制。temperature 设 0.8 平衡创意和可控性。

### 部署类
15. **Q: 前端怎么部署的？**
    A: Vercel 自动部署。GitHub push → Vercel 自动构建（npx vite build）→ 自动上线。自带 HTTPS 和 CDN。

16. **Q: 后端怎么部署的？**
    A: PythonAnywhere。免费版只支持 WSGI，用 asgiref 把 FastAPI 包装成 WSGI。数据库用 SQLite，环境变量在 Web 配置页设置。

### 综合类
17. **Q: 上线后怎么排查问题？**
    A: 前端看浏览器 Console + Network，后端看 PythonAnywhere 错误日志。健康检查接口 `/api/health` 验证后端是否活着。

18. **Q: 这个项目还有什么可以改进的？**
    A: 支付接入真实支付接口、批量生成做好异步队列、加 Redis 做缓存和限流、前端加 PWA 支持离线使用。

19. **Q: 如果用户量增长到 1 万，系统哪里会出问题？**
    A: SQLite 并发写入瓶颈（换 PostgreSQL）、PythonAnywhere 免费版性能限制（换云服务器）、AI API 调用频率限制（加队列削峰）。

20. **Q: 你从做这个项目中学到了什么？**
    A: 全栈思维——做前端时会考虑后端怎么实现，做后端时会考虑前端好不好接。还有 Prompt 工程的重要性——AI 质量取决于你怎么问。

---

## 📝 总结

20 道面试题 + 5 个技术深挖，覆盖了项目里所有核心技术点。面试前过一遍，你能自信地说出每个"为什么"。

---

> —— 阿珊，前端开发者 & AI 提效实践者
