## 1. 移除冲突依赖

- [x] 1.1 执行 `pnpm remove @tailwindcss/postcss` 从 devDependencies 中移除该包
- [x] 1.2 确认 `package.json` 中不再包含 `@tailwindcss/postcss`
- [x] 1.3 确认 `pnpm-lock.yaml` 已更新

## 2. 验证修复

- [x] 2.1 执行 `pnpm build:mp-weixin:test` 确认构建成功（无报错）
- [x] 2.2 检查 `dist/build/mp-weixin/app.wxss` 产物中不包含未处理的 `@tailwind` 指令
- [x] 2.3 执行 `pnpm dev:mp-weixin` 启动开发服务器，确认无 WXSS 编译错误
- [ ] 2.4 在微信开发者工具中导入 `dist/dev/mp-weixin`，确认首页正常渲染
