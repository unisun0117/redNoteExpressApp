/**
 * 用户状态管理 Store
 *
 * 管理用户认证状态、用户信息等
 * 具体业务逻辑在后续开发中补充
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// ---------------------------------------------------------------------------
// 类型定义（占位示例）
// ---------------------------------------------------------------------------

export interface UserInfo {
  userId: string
  userName: string
  avatar: string
  phone: string
  email: string
}

// ---------------------------------------------------------------------------
// Store 定义
// ---------------------------------------------------------------------------

export const useUserStore = defineStore('user', () => {
  // --- 状态 ---
  const token = ref<string>(uni.getStorageSync('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // --- 计算属性 ---
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.userName ?? '')

  // --- 操作 ---

  /** 设置 token（登录成功时调用） */
  function setToken(newToken: string) {
    token.value = newToken
    uni.setStorageSync('token', newToken)
  }

  /** 设置用户信息 */
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
  }

  /** 退出登录：清空 token + 用户信息，重定向到登录页 */
  function logout() {
    token.value = ''
    userInfo.value = null
    uni.removeStorageSync('token')
    // 同步清除展示姓名缓存，避免下次登录前残留旧用户名（首页 displayName 会回退读 storage）
    uni.removeStorageSync('userName')
    uni.reLaunch({ url: '/pages/login/index' })
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userName,
    setToken,
    setUserInfo,
    logout,
  }
})
