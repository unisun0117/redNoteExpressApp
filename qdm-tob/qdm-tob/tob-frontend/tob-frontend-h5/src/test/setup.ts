/**
 * 测试全局 Setup
 *
 * 职责：
 * 1. 全局注册 Element Plus（防止 "找不到 ElButton" 等组件未注册错误）
 * 2. 全局挂载 Pinia（防止 "Pinia 未激活" / getActivePinia() 报错）
 * 3. Mock 浏览器 API（matchMedia / IntersectionObserver / ResizeObserver / scrollTo）
 * 4. 挂载 @testing-library/jest-dom 扩展断言
 *
 * Vitest 配置中通过 setupFiles 自动注入，无需在每个 spec 中重复导入。
 */

import { config } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import '@testing-library/jest-dom/vitest'

// ===== Element Plus 全局注册 =====
// 挂载到 @vue/test-utils 全局插件槽位，所有 mount/shallowMount 自动注入
config.global.plugins = config.global.plugins || []
config.global.plugins.push(ElementPlus)

// ===== Pinia 全局激活 =====
// 每个 test 运行前创建全新 Pinia 实例并激活，隔离各测试间的状态污染
let activePinia: ReturnType<typeof createPinia>

beforeEach(() => {
  activePinia = createPinia()
  setActivePinia(activePinia)
})

afterEach(() => {
  // 清理 Pinia 激活状态，避免跨测试泄漏
  activePinia = undefined as unknown as ReturnType<typeof createPinia>
})

// ===== 浏览器 API Mocks（jsdom / happy-dom 未实现的 API） =====

// matchMedia（Element Plus / Vant 响应式断点依赖）
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: (query: string): MediaQueryList => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => {},
    removeListener: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
  }),
})

// IntersectionObserver（Element Plus 懒加载 / Vant LazyLoad 依赖）
if (typeof IntersectionObserver === 'undefined') {
  ;(window as unknown as Record<string, unknown>).IntersectionObserver = class {
    readonly root: Element | null = null
    readonly rootMargin = ''
    readonly thresholds: ReadonlyArray<number> = []
    constructor() {}
    observe() {}
    unobserve() {}
    disconnect() {}
    takeRecords(): IntersectionObserverEntry[] {
      return []
    }
  }
}

// ResizeObserver（Element Plus 部分组件依赖）
if (typeof ResizeObserver === 'undefined') {
  ;(window as unknown as Record<string, unknown>).ResizeObserver = class {
    constructor() {}
    observe() {}
    unobserve() {}
    disconnect() {}
  }
}

// Element.scrollTo（jsdom 不实现）
if (!Element.prototype.scrollTo) {
  Element.prototype.scrollTo = () => {}
}

// window.scrollTo
if (!window.scrollTo) {
  window.scrollTo = () => {}
}

// requestAnimationFrame（部分动画/过渡依赖）
if (!globalThis.requestAnimationFrame) {
  globalThis.requestAnimationFrame = (cb: FrameRequestCallback): number => {
    return setTimeout(cb, 0) as unknown as number
  }
}

if (!globalThis.cancelAnimationFrame) {
  globalThis.cancelAnimationFrame = (id: number) => {
    clearTimeout(id)
  }
}
