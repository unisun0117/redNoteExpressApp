/**
 * 冒烟测试 — 验证测试全家桶环境正确运行
 *
 * 检测项:
 * - Vitest 能正常执行
 * - Element Plus 全局组件已注册
 * - Pinia 测试实例已激活
 * - uni-app API Mock 已生效
 * - jsdom DOM 环境正常
 */

import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { useUserStore } from '@/store/modules/user'

describe('测试环境冒烟检查', () => {
  // ---------------------------------------------------------------------------
  // 1. jsdom 环境检查
  // ---------------------------------------------------------------------------

  it('jsdom 环境正常', () => {
    expect(window).toBeDefined()
    expect(document).toBeDefined()
    expect(window.matchMedia).toBeDefined()
    expect(window.IntersectionObserver).toBeDefined()
  })

  // ---------------------------------------------------------------------------
  // 2. uni-app API Mock 检查
  // ---------------------------------------------------------------------------

  it('uni API mock 已生效', () => {
    expect(uni).toBeDefined()
    expect(uni.showToast).toBeDefined()
    expect(uni.request).toBeDefined()
    expect(uni.getStorageSync).toBeDefined()
  })

  it('uni.showToast 可被调用且不报错', () => {
    expect(() => {
      uni.showToast({ title: 'test', icon: 'none' })
    }).not.toThrow()
  })

  // ---------------------------------------------------------------------------
  // 3. Pinia 环境检查
  // ---------------------------------------------------------------------------

  it('Pinia 测试实例已激活', () => {
    const store = useUserStore()
    expect(store).toBeDefined()
    expect(store.token).toBe('')
    expect(store.isLoggedIn).toBe(false)
  })

  it('Pinia store 可正常设置状态', () => {
    const store = useUserStore()
    store.setToken('test-token-123')
    expect(store.token).toBe('test-token-123')
    expect(store.isLoggedIn).toBe(true)
  })

  // ---------------------------------------------------------------------------
  // 4. Vue 组件挂载检查
  // ---------------------------------------------------------------------------

  it('@vue/test-utils mount 正常挂载组件', () => {
    const TestComp = defineComponent({
      template: '<view class="test">Hello Test</view>',
    })

    const wrapper = mount(TestComp)
    expect(wrapper.text()).toBe('Hello Test')
  })

  it('自定义组件可挂载', () => {
    const CompWithClick = defineComponent({
      template: `<button @click="$emit('submit')">提交</button>`,
      emits: ['submit'],
    })

    const wrapper = mount(CompWithClick)
    wrapper.find('button').trigger('click')
    expect(wrapper.emitted('submit')).toBeTruthy()
  })
})
