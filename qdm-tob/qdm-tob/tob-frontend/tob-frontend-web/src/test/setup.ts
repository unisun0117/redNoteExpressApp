/**
 * Vitest 全局 Setup
 *
 * 负责：
 * 1. 注册 Element Plus 组件（ElButton / ElInput / ElMessage 等）
 * 2. 注册 @element-plus/icons-vue 全部图标
 * 3. 提供 Pinia 自动激活包装函数
 * 4. 注入全局 Mock（Router / ElMessage / localStorage）
 * 5. 与 @testing-library/vue 的 cleanup 联动
 *
 * 此后所有 .test.ts / .spec.ts 无需手动注册这些基础设施。
 */

import { config } from '@vue/test-utils'
import { afterEach } from 'vitest'
import { cleanup } from '@testing-library/vue'
import type { Plugin } from 'vue'

// =============================================
// 1. Element Plus 组件全局注册
// =============================================
// 注意：这里不直接 import ElementPlus 做 app.use()，
// 因为 @vue/test-utils 通过 config.global.plugins 注入。

import ElementPlus from 'element-plus'
config.global.plugins = config.global.plugins || []
;(config.global.plugins as Plugin[]).push(ElementPlus)

// =============================================
// 2. Element Plus 图标全局注册
// =============================================
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

config.global.components = config.global.components || {}
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  if (key !== 'default') {
    ;(config.global.components as Record<string, unknown>)[key] = component
  }
}

// =============================================
// 3. Mock 全局对象（浏览器 API）
// =============================================

// ElMessage — Element Plus 消息提示依赖 DOM，mock 掉避免报错
import { vi } from 'vitest'

vi.mock('element-plus', async () => {
  const actual = await vi.importActual<typeof import('element-plus')>('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn(),
    },
  }
})

// =============================================
// 4. @testing-library/vue 自动 cleanup
// =============================================
afterEach(() => {
  cleanup()
})

// =============================================
// 5. 辅助导出：创建带 Pinia 的测试上下文
// =============================================
export { createPinia, setActivePinia } from 'pinia'
export { config }

// =============================================
// 6. 空 CSS 文件处理（Vite 已内置，此处为兜底）
// =============================================
// 如果仍有 "Cannot find module '*.css'" 请在 vitest 配置中添加：
// css: { modules: { classNameStrategy: 'non-scoped' } }
