## 1. 阿里云 SLS 日志监控

- [x] 1.1 安装 @aliyun-sls/web-track-mini、@aliyun-sls/web-sts-plugin、@aliyun-sls/web-types
- [x] 1.2 创建 src/utils/sls.ts — TS 封装 SLS Tracker 单例 + STS 插件 + sendSlsLog()
- [x] 1.3 修改 src/api/request.ts — 请求拦截器记录 startTime，响应/错误拦截器调用 sendSlsLog()
- [x] 1.4 system 默认值设为 'tob-小程序'

## 2. 登录页设计实现

- [x] 2.1 使用 UI/UX Pro Max 检索 Restaurant & Food 设计体系
- [x] 2.2 重写 src/pages/login/index.vue — 品牌区 + 微信登录 + 手机号登录 + 协议勾选
- [x] 2.3 品牌名改为"钱鲜达"，标语改为"欢迎使用钱鲜达"
- [x] 2.4 Logo 引用 src/static/login/logo.png

## 3. 文档更新

- [x] 3.1 README.md 新增 SLS 日志监控和 UI 设计体系章节
- [x] 3.2 OpenSpec 创建 add-sls-monitoring-and-login-page change 记录本次修改
