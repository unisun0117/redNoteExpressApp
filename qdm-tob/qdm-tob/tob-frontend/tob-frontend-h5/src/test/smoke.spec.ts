/**
 * 冒烟测试 —— 验证 Element Plus + Pinia 全局插槽生效
 *
 * 目标：确保 AI / 开发者跑测试时不会遇到：
 *   1. "Failed to resolve component: ElButton"（Element Plus 未注册）
 *   2. "getActivePinia() was called but there was no active Pinia"（Pinia 未激活）
 */

import { mount, flushPromises } from '@vue/test-utils'
import { defineComponent, h, nextTick } from 'vue'
import { createPinia, setActivePinia, defineStore } from 'pinia'
import { ElButton } from 'element-plus'
import { render, screen } from '@testing-library/vue'

describe('测试全家桶冒烟验证', () => {
  // ===== Pinia 激活验证 =====
  it('Pinia 应该在 beforeEach 中自动激活', () => {
    // auto-setup 已在 beforeEach 中完成，此处直接 defineStore 应可用
    const useTestStore = defineStore('testSmoke', {
      state: () => ({ count: 0 }),
      actions: {
        increment() {
          this.count++
        },
      },
    })

    const store = useTestStore()
    expect(store.count).toBe(0)
    store.increment()
    expect(store.count).toBe(1)
  })

  it('未手动激活 Pinia 时, 全局 setup 已激活过, Store 可正常实例化', () => {
    const useAnother = defineStore('testSmoke2', {
      state: () => ({ name: 'hello' }),
    })
    expect(useAnother().name).toBe('hello')
  })

  // ===== Element Plus 组件注册验证 =====
  it('Element Plus ElButton 应该可被 mount', () => {
    const wrapper = mount(ElButton, {
      slots: { default: '点击' },
    })
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('点击')
  })

  it('自定义组件中使用 ElButton 不应抛出 resolve 错误', () => {
    const TestComp = defineComponent({
      components: { ElButton },
      setup() {
        return () => h('div', [h(ElButton, {}, () => '确定')])
      },
    })

    const wrapper = mount(TestComp)
    expect(wrapper.findComponent(ElButton).exists()).toBe(true)
    expect(wrapper.text()).toContain('确定')
  })

  // ===== @testing-library/vue 兼容性验证 =====
  it('@testing-library/vue render 应该正常工作', () => {
    const TestComp = defineComponent({
      template: '<div data-testid="hello">Hello Test</div>',
    })

    render(TestComp)
    expect(screen.getByTestId('hello')).toBeTruthy()
    expect(screen.getByText('Hello Test')).toBeTruthy()
  })

  // ===== 浏览器 API Mocks 验证 =====
  it('matchMedia mock 应该生效', () => {
    expect(window.matchMedia).toBeDefined()
    const result = window.matchMedia('(min-width: 768px)')
    expect(result.matches).toBe(false)
  })

  it('IntersectionObserver mock 应该生效', () => {
    expect(window.IntersectionObserver).toBeDefined()
    const io = new window.IntersectionObserver(() => {})
    expect(io.observe).toBeDefined()
  })

  it('ResizeObserver mock 应该生效', () => {
    expect(window.ResizeObserver).toBeDefined()
    const ro = new window.ResizeObserver(() => {})
    expect(ro.observe).toBeDefined()
  })

  it('Element.scrollTo mock 应该生效', () => {
    const div = document.createElement('div')
    expect(() => div.scrollTo()).not.toThrow()
  })
})
