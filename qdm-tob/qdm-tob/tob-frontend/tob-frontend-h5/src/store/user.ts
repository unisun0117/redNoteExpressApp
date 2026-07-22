import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 用户状态管理（骨架）
 *
 * 后续业务开发时按需扩展字段。
 */
export const useUserStore = defineStore('user', () => {
  // ===== 状态 =====
  const token = ref<string>(sessionStorage.getItem('token') || '')
  const userInfo = ref<Record<string, unknown> | null>(null)

  // ===== 计算属性 =====
  const isLoggedIn = computed(() => !!token.value)

  // ===== 方法 =====
  function setToken(value: string) {
    token.value = value
    sessionStorage.setItem('token', value)
  }

  function clearToken() {
    token.value = ''
    sessionStorage.removeItem('token')
  }

  function setUserInfo(info: Record<string, unknown> | null) {
    userInfo.value = info
    if (info) {
      sessionStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      sessionStorage.removeItem('userInfo')
    }
  }

  function logout() {
    clearToken()
    setUserInfo(null)
  }

  return { token, userInfo, isLoggedIn, setToken, clearToken, setUserInfo, logout }
})
