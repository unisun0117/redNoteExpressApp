const ENV = import.meta.env.VITE_APP_ENV || 'qa'
const CAS_BASE_URL = import.meta.env.VITE_APP_CAS_URL
const WEB_BASE_URL = import.meta.env.VITE_APP_WEB_URL
const MY_BASE_URL = import.meta.env.VITE_MY_BASE_URL
const PORT = import.meta.env.VITE_APP_PORT

interface EnvConfig {
  PORT: string
  ENV: string
  /** API 接口基础地址 */
  BASE_URL: string
  /** CAS 统一登录地址 */
  CAS_BASE_URL: string
  /** 项目 PC 端访问地址（CAS 登录后回调） */
  WEB_BASE_URL: string
  /** 峰景台地址 */
  MY_BASE_URL: string
  /** H5 请求地址 */
  H5_BASE_URL: string
}

/** 当前环境配置 */
export const envConfig: EnvConfig = {
  PORT,
  ENV,
  BASE_URL: '',
  CAS_BASE_URL,
  WEB_BASE_URL,
  MY_BASE_URL,
  H5_BASE_URL: '',
}

/** 是否为本地开发环境 */
export const isLocalDev = !ENV || ENV === 'dev' || ENV === 'development' || import.meta.env.DEV

/**
 * 获取 CAS 登录后的回调地址（可选携带重定向路径）
 * 本地开发 → http://localhost:8080/home（或 http://localhost:8080/xxx）
 * 其他环境 → WEB_BASE_URL + /home（或 WEB_BASE_URL + /xxx）
 */
export function getServiceUrl(redirect?: string): string {
  const path = redirect || '/home'
  if (isLocalDev) {
    return `${WEB_BASE_URL}:${PORT}${path}`
  }
  return `${WEB_BASE_URL}${path}`
}

/**
 * 获取 CAS 登录页完整 URL
 * @param redirect 登录后跳转路径，如 /todo/list
 */
export function getCasLoginUrl(redirect?: string): string {
  return `${CAS_BASE_URL}/login?service=${encodeURIComponent(getServiceUrl(redirect))}`
}

// ===== 应用凭证（OAuth2 client_id / client_secret） =====
// TODO: 替换为实际应用凭证
export const APP_CREDENTIALS = {
  CLIENT_ID: 'cas',
  CLIENT_SECRET: 'cas_sec',
}
/**
 * 获取 CAS 登出 URL，登出后回调到当前项目首页
 */
export function getCasLogoutUrl(): string {
  return `${CAS_BASE_URL}/logout?service=${encodeURIComponent(getServiceUrl('/home'))}`
}
