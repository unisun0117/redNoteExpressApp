## Context

项目使用 Tailwind CSS v3（`tailwindcss: ^3.4.0`）作为原子化 CSS 框架，`postcss.config.js` 通过 `tailwindcss: {}` 插件名引用 v3 的 PostCSS 插件。但 `devDependencies` 中同时安装了 `@tailwindcss/postcss: ^4.0.0`（实际安装 v4.3.1），该包是 Tailwind CSS v4 的 PostCSS 插件，与 v3 完全不兼容。

`@tailwindcss/postcss` v4 具备完整的 ESM `exports` 字段（`import`/`require` 双通道），而 `tailwindcss` v3 仅有 CJS `main` 入口。项目 `package.json` 配置 `"type": "module"`，在 ESM 模块解析环境下，Vite 解析 `tailwindcss` 插件时可能因 `@tailwindcss/postcss` 的存在产生解析干扰。

`postcss.config.js` 和 `tailwind.config.js` 以及所有源码均使用 v3 的 API（`@tailwind` 指令、`tailwind.config.js` 的 `content`/`theme`/`corePlugins` 结构），`@tailwindcss/postcss` v4 在项目中完全没有被引用。

## Goals / Non-Goals

**Goals:**
- 移除 `@tailwindcss/postcss` v4 依赖，消除与 `tailwindcss` v3 的模块解析冲突
- 确保 uni-app 构建产物中 `@tailwind` 指令被正确处理为实际 CSS
- 修复微信开发者工具中 `app.wxss` 的 WXSS 编译错误

**Non-Goals:**
- 不升级到 Tailwind CSS v4（需要大量配置迁移，风险较高）
- 不修改 `postcss.config.js` 或 `tailwind.config.js` 配置
- 不修改任何业务代码

## Decisions

**决策 1：移除 `@tailwindcss/postcss` v4，仅保留 `tailwindcss` v3**

- 理由：`postcss.config.js` 始终使用 `tailwindcss: {}`（v3 插件名），`@tailwindcss/postcss` v4 从未被引用。该包是误引入的，移除可消除潜在模块解析冲突。
- 替代方案考虑：
  - **升级到 Tailwind CSS v4** — 需要重写 `tailwind.config.js` 为 CSS-first 配置、更改 `@tailwind` 指令为 `@import "tailwindcss"`、更新 `postcss.config.js` 使用 `@tailwindcss/postcss` 插件。工作量大，风险高，不适合作为紧急修复。

**决策 2：在 `vite.config.ts` 中显式配置 `css.postcss.plugins`**（实际修复）

- 理由：uni-app 小程序构建模式下，Vite 不会自动加载 `postcss.config.js` 来处理 Vue SFC `<style>` 块中的 CSS。`@tailwind` 指令从未被 PostCSS 处理就进入最终产物。通过在 `vite.config.ts` 的 `css.postcss.plugins` 中显式声明 `tailwindcss()` 和 `autoprefixer()` 插件，强制 Vite 在 CSS 管线中调用这些插件。
- 配置变更：
  ```ts
  css: {
    postcss: {
      plugins: [
        tailwindcss() as any,
        autoprefixer(),
      ],
    },
  },
  ```
- 替代方案考虑：
  - **保留 postcss.config.js 自动加载** — 无效，uni-app 小程序模式不走该路径
  - **使用 vite-plugin-uni-tailwind 处理** — 该插件仅做类名/选择器转换，不处理 `@tailwind` 指令

**决策 3：修复后通过 `pnpm build:mp-weixin:test` 验证**

- 理由：build 命令明确指定 `-p mp-weixin`，产生与微信开发者工具相同的编译产物。验证标准：产物中无未注释的 `@tailwind` 指令，且 Tailwind 工具类（如 `.mb-2`、`.flex`）正常生成。

## Risks / Trade-offs

- 风险：如果 `@tailwindcss/postcss` 被其他依赖间接引用（例如 `@uni-helper/vite-plugin-uni-tailwind`），移除后可能导致那些依赖找不到包
  - 缓解：已验证两个 uni-app 相关包（`@dcloudio/vite-plugin-uni`、`@uni-helper/vite-plugin-uni-tailwind`）的源码，均不依赖 `@tailwindcss/postcss`。`vite-plugin-uni-tailwind` 的 peerDependencies 仅要求 `tailwindcss: ^3.0.0`

- 风险：清除依赖后 pnpm lockfile 变化可能引入其他包版本漂移
  - 缓解：仅执行 `pnpm remove @tailwindcss/postcss`，pnpm 会保持其他包版本不变
