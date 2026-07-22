/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  // 按环境输出到不同目录
  const outDirMap: Record<string, string> = {
    test: 'dist-qa',
    production: 'dist-pro',
  }

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
      },
    },
    build: {
      outDir: outDirMap[mode] || 'dist',
    },
    server: {
      port: 5173,
      host: '0.0.0.0',
      proxy: {
        '/api': {
          target: 'https://ebwmstest.qdama.cn:19011',
          changeOrigin: true,
          secure: false,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
      },
    },
    css: {
      postcss: './postcss.config.js',
    },

    // ===== Vitest 测试配置 =====
    test: {
      environment: 'jsdom',
      globals: true,
      setupFiles: ['./src/test/setup.ts'],
      css: false,
      include: ['src/**/*.{test,spec}.{ts,tsx}'],
      coverage: {
        provider: 'v8',
        reporter: ['text', 'json', 'html', 'lcov'],
        include: ['src/**/*.{ts,tsx,vue}'],
        exclude: [
          'src/test/**',
          'src/**/*.d.ts',
          'src/**/*.spec.ts',
          'src/**/*.test.ts',
        ],
      },
      server: {
        deps: {
          inline: ['element-plus', 'vant'],
        },
      },
    },
  }
})
