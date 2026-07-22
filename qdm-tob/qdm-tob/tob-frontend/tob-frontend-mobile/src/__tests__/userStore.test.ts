/**
 * userStore 单元测试
 *
 * 重点覆盖两个线上 bug 的回归：
 * 1. 退出登录后 storage 里 `userName` 残留 → 换号登录首页显示旧用户名
 *    （logout 必须同时清 token 与 userName）
 * 2. 登录成功后从 token 写入 userName（依赖 utils/jwt 解析，见 jwt.test.ts）
 *
 * setup.ts 的 uni storage mock 是 no-op（getStorageSync 恒返回 ''），
 * 这两个 bug 的核心是 storage 残留，故这里用内存 map 还原真实 storage 行为。
 *
 * 本测试不依赖 DOM，跑在 node 环境以绕开 jsdom 29 + Node 18 的 ERR_REQUIRE_ESM。
 */

// @vitest-environment node

import { beforeEach, describe, it, expect, vi } from 'vitest'
import { useUserStore } from '@/store/modules/user'

/** 用内存 map 模拟真实 storage 读写，使 getStorageSync 能读到 setStorageSync 写入的值 */
function enableMemoryStorage() {
  const map = new Map<string, unknown>()
  const uniObj = uni as unknown as Record<string, (...args: unknown[]) => unknown>
  uniObj.getStorageSync = vi.fn((k: string) => (map.has(k) ? map.get(k) : ''))
  uniObj.setStorageSync = vi.fn((k: string, v: unknown) => {
    map.set(k, v)
  })
  uniObj.removeStorageSync = vi.fn((k: string) => {
    map.delete(k)
  })
  // reLaunch 在 setup.ts 已是 vi.fn，这里清掉调用历史，便于断言
  vi.mocked(uni.reLaunch as unknown as (...args: unknown[]) => unknown).mockClear()
  return map
}

describe('userStore', () => {
  beforeEach(() => {
    enableMemoryStorage()
  })

  // ------------------------------------------------------------------
  // setToken
  // ------------------------------------------------------------------

  describe('setToken', () => {
    it('写入 store.token 并持久化到 storage', () => {
      const store = useUserStore()
      store.setToken('token-A')
      expect(store.token).toBe('token-A')
      expect(uni.getStorageSync('token')).toBe('token-A')
      expect(store.isLoggedIn).toBe(true)
    })
  })

  // ------------------------------------------------------------------
  // setUserInfo
  // ------------------------------------------------------------------

  describe('setUserInfo', () => {
    it('设置 userInfo.userName，供首页 displayName 读取', () => {
      const store = useUserStore()
      store.setUserInfo({
        userId: '2',
        userName: '李雯琪',
        avatar: '',
        phone: '15778886519',
        email: '',
      })
      expect(store.userInfo?.userName).toBe('李雯琪')
      expect(store.userName).toBe('李雯琪')
    })
  })

  // ------------------------------------------------------------------
  // logout —— 核心回归
  // ------------------------------------------------------------------

  describe('logout', () => {
    it('清除 token 与 userName 两个 storage key，杜绝换号登录后显示旧用户名', () => {
      // 回归场景：注册/登录 A（李大琪）→ 退出 → 微信登录 B（李雯琪），
      // 首页曾因 storage 残留 '李大琪' 且新登录 userInfo.userName 为空而错显 A
      const store = useUserStore()
      store.setToken('token-A')
      uni.setStorageSync('userName', '李大琪')
      expect(uni.getStorageSync('userName')).toBe('李大琪')

      store.logout()

      expect(store.token).toBe('')
      expect(store.userInfo).toBeNull()
      expect(store.isLoggedIn).toBe(false)
      expect(uni.getStorageSync('token')).toBe('')
      // 修复前 logout 只清 token，userName 残留 → 首页 displayName 回退读到旧名字
      expect(uni.getStorageSync('userName')).toBe('')
    })

    it('reLaunch 到登录页', () => {
      const store = useUserStore()
      store.logout()
      expect(uni.reLaunch).toHaveBeenCalledWith({ url: '/pages/login/index' })
    })
  })
})
