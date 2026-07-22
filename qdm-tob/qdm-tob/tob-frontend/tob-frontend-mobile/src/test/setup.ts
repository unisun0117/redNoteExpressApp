/**
 * Vitest 全局测试环境 Setup
 *
 * 在每个测试文件运行前执行，确保:
 * - Element Plus 组件全局注册（避免 "找不到 ElButton" 等报错）
 * - Pinia 状态管理已激活（避免 "Pinia 未激活" 报错）
 * - uni-app 小程序 API 已 Mock（避免 "uni is not defined" 报错）
 * - 浏览器 API 已 polyfill（jsdom 缺失的属性和方法）
 *
 * 使用方式:
 *   无需手动 import，vite.config.ts 中已配置 setupFiles
 *
 * @see https://vitest.dev/config/#setupfiles
 */

import { config } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createTestingPinia } from '@pinia/testing'
import ElementPlus from 'element-plus'
import { beforeEach, afterEach, vi } from 'vitest'

// ---------------------------------------------------------------------------
// 1. 注册 Element Plus 全局组件
// ---------------------------------------------------------------------------

// 通过 @vue/test-utils 的 global plugins 机制全局注册
// 避免每个测试文件都要手动 app.use(ElementPlus)
config.global.plugins = config.global.plugins || []
config.global.plugins.push(ElementPlus)

// ---------------------------------------------------------------------------
// 2. Pinia 测试实例（每个测试用例自动重置）
// ---------------------------------------------------------------------------

let testingPinia: ReturnType<typeof createTestingPinia> | null = null

beforeEach(() => {
  // 创建新的 testing Pinia 实例（初始状态为空，支持 stubActions）
  testingPinia = createTestingPinia({
    // stubActions 为 true 时，所有 action 自动替换为 vi.fn()
    stubActions: false,
    // 可以在这里预设初始 state
    initialState: {},
  })

  // 激活 Pinia 实例，避免 "Pinia 未激活" 报错
  setActivePinia(testingPinia)

  // 将 testing pinia 挂到 global plugins 上
  config.global.plugins = [
    ElementPlus,
    testingPinia,
  ]
})

afterEach(() => {
  testingPinia = null
})

// ---------------------------------------------------------------------------
// 3. uni-app 小程序 API Mock
//    确保在 jsdom 环境下不报 "uni is not defined"
// ---------------------------------------------------------------------------

// 定义全局 uni 对象，mock 微信小程序常用 API
;(globalThis as Record<string, unknown>).uni = {
  // --- 存储 ---
  getStorageSync: vi.fn((_key: string) => ''),
  setStorageSync: vi.fn((_key: string, _value: unknown) => {}),
  removeStorageSync: vi.fn((_key: string) => {}),

  // --- 界面交互 ---
  showToast: vi.fn((_options: Record<string, unknown>) => {}),
  showLoading: vi.fn((_options: Record<string, unknown>) => {}),
  hideLoading: vi.fn(() => {}),
  showModal: vi.fn((_options: Record<string, unknown>) => {}),

  // --- 导航 ---
  navigateTo: vi.fn((_options: Record<string, unknown>) => {}),
  redirectTo: vi.fn((_options: Record<string, unknown>) => {}),
  switchTab: vi.fn((_options: Record<string, unknown>) => {}),
  reLaunch: vi.fn((_options: Record<string, unknown>) => {}),
  navigateBack: vi.fn((_options?: Record<string, unknown>) => {}),

  // --- 网络 ---
  request: vi.fn((_options: Record<string, unknown>) => {}),
  uploadFile: vi.fn((_options: Record<string, unknown>) => {}),

  // --- 其他 ---
  getSystemInfoSync: vi.fn(() => ({
    platform: 'devtools',
    windowWidth: 375,
    windowHeight: 667,
  })),
}

// ---------------------------------------------------------------------------
// 4. 微信小程序 API Mock（jsdom 中不存在 wx 全局对象）
// ---------------------------------------------------------------------------

;(globalThis as Record<string, unknown>).wx = {
  getAccountInfoSync: vi.fn(() => ({
    miniProgram: { version: '1.0.0' },
  })),
  request: vi.fn((_options: Record<string, unknown>) => {}),
}

// ---------------------------------------------------------------------------
// 5. jsdom 缺失的浏览器 API Polyfill
//    node 环境下（@vitest-environment node 的测试）无 window，整体跳过，
//    仅 jsdom 环境下才需要这些 polyfill。
// ---------------------------------------------------------------------------

if (typeof window !== 'undefined') {
  // matchMedia (Element Plus 响应式组件内部使用)
  if (!window.matchMedia) {
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation((query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
      })),
    })
  }

  // IntersectionObserver (被部分组件使用)
  if (!window.IntersectionObserver) {
    ;(window as Record<string, unknown>).IntersectionObserver = vi
      .fn()
      .mockImplementation(() => ({
        observe: vi.fn(),
        unobserve: vi.fn(),
        disconnect: vi.fn(),
      }))
  }

  // getComputedStyle 返回基础值（避免 CSS 变量报错）
  const originalGetComputedStyle = window.getComputedStyle
  window.getComputedStyle = (element: Element, pseudoElt?: string | null) => {
    const style = originalGetComputedStyle(element, pseudoElt)
    // 确保 CSS 自定义属性不会抛出错误
    return new Proxy(style, {
      get(target, prop) {
        if (typeof prop === 'string' && prop.startsWith('--')) {
          return ''
        }
        return Reflect.get(target, prop)
      },
    })
  }

  // scrollTo (jsdom 不支持滚动)
  if (!window.scrollTo) {
    window.scrollTo = vi.fn() as unknown as typeof window.scrollTo
  }

  // requestAnimationFrame
  if (!window.requestAnimationFrame) {
    window.requestAnimationFrame = vi.fn((cb: FrameRequestCallback) => {
      setTimeout(cb, 0)
      return 0
    })
  }

  // ResizeObserver
  if (!window.ResizeObserver) {
    ;(window as Record<string, unknown>).ResizeObserver = vi
      .fn()
      .mockImplementation(() => ({
        observe: vi.fn(),
        unobserve: vi.fn(),
        disconnect: vi.fn(),
      }))
  }
}
