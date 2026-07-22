import { defineConfig } from 'vite'
import uniModule from '@dcloudio/vite-plugin-uni'
import uniTailwind from '@uni-helper/vite-plugin-uni-tailwind'
import tailwindcss from 'tailwindcss'
import autoprefixer from 'autoprefixer'
import path from 'path'

// @dcloudio/vite-plugin-uni 在 ESM 下 default import 返回的是
// CJS module.exports 对象，实际插件函数在 .default 属性上
const uni = (uniModule as unknown as Record<string, unknown>).default || uniModule

// https://vitejs.dev/config/
// https://vitest.dev/config/
export default defineConfig({
  plugins: [
    // uniTailwind 必须在 uni 之前，否则 @tailwind 指令不会被处理
    uniTailwind(),
    uni(),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  css: {
    postcss: {
      plugins: [
        tailwindcss() as any,
        autoprefixer(),
      ],
    },
    // 静默 Sass @import 和 legacy-js-api 废弃警告
    // 原因：uview-plus 内部仍使用 @import，无法由项目侧改为 @use
    preprocessorOptions: {
      scss: {
        silenceDeprecations: ['import', 'legacy-js-api'],
      },
    },
  },
  // ---------------------------------------------------------------------------
  // Vitest 测试配置
  // ---------------------------------------------------------------------------
  test: {
    // jsdom 模拟浏览器 DOM 环境
    environment: 'jsdom',
    // 全局 setup 文件：注册 Element Plus + Pinia + uni-app mocks
    setupFiles: [path.resolve(__dirname, 'src/test/setup.ts')],
    // 开启全局测试 API（无需 import { describe, it, expect }）
    globals: true,
    // CSS 模块在测试中按原始类名返回，避免 snapshot 不稳定
    css: {
      modules: {
        classNameStrategy: 'non-scoped',
      },
    },
    // 排除不需要测试的目录
    exclude: [
      'node_modules',
      'dist',
      'openspec',
    ],
    // 覆盖率配置（可选，后续按需开启）
    coverage: {
      enabled: false,
      provider: 'v8',
      include: ['src/**/*.{ts,vue}'],
      exclude: [
        'src/**/*.d.ts',
        'src/test/**',
        'src/uni.scss',
      ],
    },
  },
})
