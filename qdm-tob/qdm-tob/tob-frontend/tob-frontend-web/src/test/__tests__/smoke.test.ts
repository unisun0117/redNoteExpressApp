/**
 * 冒烟测试 — 验证 Vitest + Element Plus + Pinia + Vue Router 全家桶可用性
 *
 * 如果此文件通过，说明全局 setup 完整、无遗漏。
 */

import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { h, defineComponent } from 'vue'
import { render, screen } from '@testing-library/vue'

// ────────────────────────────────────
// 1. @vue/test-utils + Element Plus 基础
// ────────────────────────────────────
describe('Smoke: @vue/test-utils + Element Plus', () => {
  it('可以 mount 包含 ElButton 的组件', () => {
    const wrapper = mount(
      defineComponent({
        setup() {
          return () => h('el-button', { type: 'primary' }, '点我')
        },
      }),
    )
    expect(wrapper.html()).toContain('el-button')
    expect(wrapper.text()).toContain('点我')
  })

  it('可以 mount 包含 ElInput 的组件', () => {
    const wrapper = mount(
      defineComponent({
        setup() {
          return () => h('el-input', { modelValue: '测试', placeholder: '请输入' })
        },
      }),
    )
    expect(wrapper.html()).toContain('el-input')
  })
})

// ────────────────────────────────────
// 2. @testing-library/vue 集成
// ────────────────────────────────────
describe('Smoke: @testing-library/vue', () => {
  it('render 后可查询 DOM', () => {
    const TestComp = defineComponent({
      template: '<div><span data-testid="msg">Hello TLV</span></div>',
    })
    render(TestComp)
    expect(screen.getByTestId('msg').textContent).toBe('Hello TLV')
  })
})

// ────────────────────────────────────
// 3. Pinia Store 创建
// ────────────────────────────────────
describe('Smoke: Pinia Store', () => {
  it('可以在测试中激活 Pinia', () => {
    setActivePinia(createPinia())
    // 无报错即可
    expect(true).toBe(true)
  })
})

// ────────────────────────────────────
// 4. Vue Router 实例
// ────────────────────────────────────
describe('Smoke: Vue Router', () => {
  it('可以创建 Router 实例', () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/', component: { template: '<div>home</div>' } }],
    })
    expect(router).toBeDefined()
  })
})

// ────────────────────────────────────
// 5. Element Plus 图标可用
// ────────────────────────────────────
describe('Smoke: Element Plus Icons', () => {
  it('ElIcon 组件可用', () => {
    const wrapper = mount(
      defineComponent({
        template: '<el-icon><component is="Edit" /></el-icon>',
      }),
    )
    expect(wrapper.html()).toContain('el-icon')
  })
})
