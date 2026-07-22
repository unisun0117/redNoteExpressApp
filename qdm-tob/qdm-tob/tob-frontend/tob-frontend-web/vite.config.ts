/// <reference types="vitest/config" />

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_')

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
      },
    },
    server: {
      port: Number(env.VITE_APP_PORT) || 8080,
      open: true,
      proxy: {
        // 业务 API → 本地后端（端口 8081）
        '/api': {
          target: 'http://localhost:8081',
          changeOrigin: true,
        },
        '/oauth2': {
          target: 'http://localhost:8081',
          changeOrigin: true,
        },
      },
    },
    build: {
      outDir: mode === 'production' ? 'dist-pro' : mode === 'test' ? 'dist-qa' : 'dist',
      sourcemap: false,
    },

    // ===== Vitest 配置 =====
    test: {
      // jsdom 模拟浏览器 DOM
      environment: 'jsdom',

      // 全局 setup 文件：注册 Element Plus / Pinia / 图标
      setupFiles: ['./src/test/setup.ts'],

      // CSS 导入内联处理，避免测试时报 "Unknown file extension .css"
      css: {
        modules: {
          classNameStrategy: 'non-scoped',
        },
      },

      // 路径别名（匹配 tsconfig paths）
      alias: {
        '@': resolve(__dirname, 'src'),
      },

      // 全局 API（可选：define, it, expect 无需显式 import）
      globals: true,

      // 覆盖率配置（按需启用）
      coverage: {
        provider: 'v8',
        reporter: ['text', 'json', 'html'],
        include: ['src/**/*.{ts,tsx,vue}'],
        exclude: ['src/test/**', 'src/env.d.ts'],
      },
    },
  }
})
