/**
 * Token 管理 — localStorage 存储
 * Key: Token（与参考项目一致）
 */

const TOKEN_KEY = 'Token'
const REAL_NAME_KEY = 'realName'
const MOBILE_KEY = 'mobile'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function getRealName(): string {
  return localStorage.getItem(REAL_NAME_KEY) || ''
}

export function setRealName(name: string): void {
  localStorage.setItem(REAL_NAME_KEY, name)
}

export function getMobile(): string {
  return localStorage.getItem(MOBILE_KEY) || ''
}

export function setMobile(mobile: string): void {
  localStorage.setItem(MOBILE_KEY, mobile)
}

/** 清除所有登录态 */
export function clearAuth(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REAL_NAME_KEY)
  localStorage.removeItem(MOBILE_KEY)
}
