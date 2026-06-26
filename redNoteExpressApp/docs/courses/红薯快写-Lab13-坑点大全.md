# Lab 13：坑点大全 — 项目里所有坑一次解决

> **预计用时：** 20-25 分钟（随时查阅）
> **难度：** ⭐
> **前置 Lab：** Lab 01-12 全覆盖

---

## 📌 前言

做项目最怕的不是遇到坑，是遇到坑不知道找谁问。这个 Lab 把红薯快写项目里我踩过的所有坑全列出来——从环境配置到部署上线，一个不漏。

建议你现在扫一遍，不需要记住。以后遇到报错，直接来这个 Lab Ctrl+F 搜关键词。

---

## 📚 环境配置坑

### 1. bcrypt 版本不兼容
- **现象：** `pip install passlib` 后运行报 `MissingBackendError` 或 `Unknown bcrypt algorithm`
- **原因：** passlib 不兼容 bcrypt 5.x，只支持 4.0.1
- **解决：** `pip install bcrypt==4.0.1`

### 2. 系统 uvicorn vs venv uvicorn
- **现象：** 启动后端报 `ModuleNotFoundError: No module named 'app'`
- **原因：** 用了系统全局的 uvicorn，它找不到 venv 里安装的依赖
- **解决：** 必须用 `.\venv\Scripts\uvicorn.exe`

### 3. pip 安装超时
- **现象：** `pip install` 一直转圈或报 timeout
- **原因：** 国内网络到 PyPI 慢
- **解决：** `pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple`

### 4. venv 激活失败（PowerShell）
- **现象：** PowerShell 报"无法加载文件，因为在此系统上禁止运行脚本"
- **原因：** PowerShell 执行策略限制
- **解决：** 用 CMD 终端，或执行 `Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned`

### 5. 端口被占用
- **现象：** `Address already in use`
- **原因：** 8000 端口已经有程序在用了
- **解决：** 换端口 `--port 8001`，或 `netstat -ano | findstr :8000` 找到占用的进程关掉

---

## 📚 后端坑

### 6. SQLite 路径错误（PythonAnywhere）
- **现象：** `OperationalError: unable to open database file`
- **原因：** PythonAnywhere 上 SQLite 路径必须 4 个斜杠
- **解决：** `sqlite:////home/username/rednote.db`（不是 3 个斜杠！）

### 7. CORS 跨域报错
- **现象：** 浏览器 Console 里 `Access-Control-Allow-Origin` 错误
- **原因：** 后端 CORS 配置没允许前端域名
- **解决：** `config.py` 里 `ALLOWED_ORIGINS` 加上前端地址，或设 `*`

### 8. JWT Token 过期
- **现象：** 接口返回 401
- **原因：** Access Token 只有 15 分钟有效
- **解决：** 前端 `authFetch` 会自动用 Refresh Token 刷新，不用手动处理

### 9. DeepSeek API 401
- **现象：** `Incorrect API key`
- **原因：** API Key 配错了或没配
- **解决：** 检查 `.env` 文件里的 `OPENAI_API_KEY`，注意是 DeepSeek 的 Key 不是 OpenAI 的

### 10. DeepSeek 余额不足
- **现象：** `Insufficient balance`
- **原因：** 免费额度用完了
- **解决：** 去 platform.deepseek.com 充值（价格很低）

### 11. JSON 解析失败
- **现象：** `json.loads` 报错 `JSONDecodeError`
- **原因：** AI 返回的内容不是合法 JSON（有时会多输出文字）
- **解决：** 检查 `response_format={"type": "json_object"}` 参数是否正确设置

---

## 📚 前端坑

### 12. TypeScript 编译报错（Vercel 构建）
- **现象：** `tsc -b` 报 `verbatimModuleSyntax` 错误
- **原因：** TypeScript 6.0 的严格模式和新语法不兼容
- **解决：** Vercel Build Command 用 `npx vite build` 跳过类型检查

### 13. 401 后无限循环
- **现象：** 页面反复跳转登录页
- **原因：** Refresh Token 也过期了，但前端还在重试
- **解决：** `authFetch` 里已经处理了——刷新失败就清 token 跳登录，不会死循环

### 14. VITE_API_URL 没配
- **现象：** 前端页面打开了但所有 API 请求失败
- **原因：** 生产环境没配 `VITE_API_URL`
- **解决：** Vercel Settings → Environment Variables 里加上

### 15. SPA 路由 404
- **现象：** 直接访问 `/login` 显示 404
- **原因：** 单页应用的路由需要 fallback 到 `index.html`
- **解决：** 项目已经有 `vercel.json` 配置了 SPA fallback

### 16. Clipboard API 不可用
- **现象：** 复制按钮没反应
- **原因：** 非 HTTPS 或非 localhost 环境限制
- **解决：** 生产环境 Vercel 自带 HTTPS，没问题

---

## 📚 部署坑

### 17. Vercel 根目录没配
- **现象：** Vercel 构建失败，找不到 `package.json`
- **原因：** Root Directory 应该指向 `redNoteExpressApp/frontend`
- **解决：** Vercel Settings → General → Root Directory

### 18. PythonAnywhere Reload 忘了点
- **现象：** 改了代码但线上没变化
- **原因：** PythonAnywhere 不会自动重启
- **解决：** 每次改完代码去 Web 页面点 Reload

### 19. PythonAnywhere 错误不显示
- **现象：** 页面显示 500 但不知道原因
- **原因：** 生产环境 Debug 模式关掉了
- **解决：** 看 PythonAnywhere 错误日志：Web 页面 → Log files → Error log

### 20. git push 超时
- **现象：** `git push` 一直卡住
- **原因：** 国内网络到 GitHub 不稳定
- **解决：** 过一会重试，或者用 PythonAnywhere 的 Bash 终端执行 git push

---

## 📝 总结

20 个坑，覆盖了全栈开发的常见问题。以后再遇到新坑，记下来，加入这个清单。这就是你的私人知识库——比任何通用教程都有价值。

---

> —— 阿珊，前端开发者 & AI 提效实践者
