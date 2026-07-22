## Why

微信小程序构建产物 `app.wxss` 中包含未处理的 `@tailwind components;` 和 `@tailwind utilities;` 指令，导致微信开发者工具报 WXSS 编译错误 `unexpected token ';'`，页面空白无法加载。根因是 `@tailwindcss/postcss` v4.3.1 与 `tailwindcss` v3.4.19 存在模块解析冲突，导致 tailwindcss PostCSS 插件在 uni-app 小程序构建链路中未被正确调用。

## What Changes

- 从 `devDependencies` 中移除未使用的 `@tailwindcss/postcss` v4.3.1（`postcss.config.js` 使用 `tailwindcss` 插件名，实际依赖的是 `tailwindcss` v3）
- 重新安装依赖以清理 pnpm lockfile
- 验证 `pnpm build:mp-weixin:test` 产物中不再包含未处理的 `@tailwind` 指令
- 验证 `pnpm dev:mp-weixin` 启动后微信开发者工具不再报 WXSS 编译错误

## Capabilities

### New Capabilities
<!-- This is a dependency fix, no new functional capabilities introduced -->

### Modified Capabilities
<!-- No existing capability requirements are changing -->

## Impact

- 受影响文件：`package.json`（移除 `@tailwindcss/postcss`）、`pnpm-lock.yaml`（重新生成）
- 无 API 变更、无 breaking changes
- 构建产物 `dist/build/mp-weixin/app.wxss` 和 `dist/dev/mp-weixin/app.wxss` 将正确包含 Tailwind CSS 编译后的样式
