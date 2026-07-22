## Why

小程序需要两个关键能力：1）接口请求失败时自动上报错误日志到阿里云 SLS，便于问题排查和监控；2）一个符合生鲜零食品牌调性的现代化登录页面。

## What Changes

- 集成阿里云 SLS 日志服务（`@aliyun-sls/web-track-mini` + `@aliyun-sls/web-sts-plugin`），STS 临时凭证自动刷新
- 封装 TypeScript 版 `sendSlsLog()` 工具函数（`src/utils/sls.ts`），`system` 默认 `'tob-小程序'`
- HTTP 请求/响应拦截器中接入 SLS：业务错误和网络异常均自动上报日志
- 登录页面从占位模板重写为完整的 Restaurant & Food 风格设计（品牌名：钱鲜达）
- 登录页包含：微信一键登录、手机号+验证码登录、用户协议勾选、品牌 Logo

## Capabilities

### New Capabilities

- `sls-logging`: 阿里云 SLS 日志监控，接口请求失败时自动上报错误日志
- `login-page`: 生鲜零食小程序登录页面，Restaurant & Food 设计风格

### Modified Capabilities
<!-- No existing capabilities modified -->

## Impact

- 新增依赖：`@aliyun-sls/web-track-mini`、`@aliyun-sls/web-sts-plugin`、`@aliyun-sls/web-types`
- 新增文件：`src/utils/sls.ts`
- 修改文件：`src/api/request.ts`（SLS 上报）、`src/pages/login/index.vue`（登录页重写）
- 设计资产：`src/static/login/logo.png`
- 无 breaking changes
