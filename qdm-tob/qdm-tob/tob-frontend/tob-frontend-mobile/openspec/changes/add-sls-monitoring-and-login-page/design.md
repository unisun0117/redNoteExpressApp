## Context

项目启动阶段，需要建立错误日志监控体系和品牌化的登录页面。两项工作独立但有依赖关系：登录页的 API 调用需要 SLS 上报能力。

## Goals / Non-Goals

**Goals:**
- 集成阿里云 SLS，接口失败时自动上报（无需业务代码手动调用）
- 登录页具备完整交互：微信登录、手机验证码登录、协议勾选、表单校验
- 登录页视觉符合生鲜零食品牌调性

**Non-Goals:**
- 不在此 change 中对接真实的微信登录 API（仅 UI 就绪）
- 不在此 change 中对接真实的短信验证码接口

## Decisions

**决策 1：SLS 使用单例 Tracker + STS 插件模式**

- 理由：参考阿里云 SLS 小程序 SDK 最佳实践，单例避免重复初始化，STS 插件自动管理凭证刷新
- STS 凭证接口：`https://ebwmstest.qdama.cn:19011/apply/api/common/sls/credential`
- `system` 字段默认值改为 `'tob-小程序'`

**决策 2：在 HTTP 拦截器中自动上报，而非业务层手动调用**

- 理由：覆盖所有接口错误，不漏报；减少业务代码侵入
- 上报时机：响应拦截器中 `code !== 0 && code !== 200`（业务错误）和网络异常（4xx/5xx/timeout）
- 上报字段：`url`、`statusCode`、`errorResponseBody`、`requestTime`

**决策 3：登录页采用 UI/UX Pro Max 的 Restaurant & Food 风格**

- 配色：食欲红 `#DC2626` + 暖金 `#CA8A04`，暖色渐变背景
- 布局：单列垂直居中，品牌 Logo → 微信登录（主 CTA）→ 分隔线 → 手机号登录
- Logo：引用 `src/static/login/logo.png`
- 品牌名：钱鲜达，标语：欢迎使用钱鲜达

**决策 4：微信按钮用暖金，登录按钮用食欲红**

- 微信一键登录作为主操作，暖金色更抢眼、更有"点击欲"
- 手机号登录按钮用食欲红，与品牌主色一致

## Risks / Trade-offs

- 风险：SLS STS 凭证接口可能不可达 → 缓解：`sendSlsLog` 内部 catch 静默失败，不影响业务流程
- 风险：`wx.getAccountInfoSync()` 非微信环境不可用 → 缓解：`#ifdef MP-WEIXIN` 条件编译 + try/catch
