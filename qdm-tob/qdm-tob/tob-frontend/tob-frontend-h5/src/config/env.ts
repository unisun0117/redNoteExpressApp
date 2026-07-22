/**
 * 环境配置 — 企微 H5 端
 * 根据 VITE_APP_ENV 自动切换各环境 URL
 */
const ENV = import.meta.env.VITE_APP_ENV || 'qa'

interface EnvConfig {
  /** API 接口基础地址 */
  BASE_URL: string
  /** CAS 统一登录地址 */
  CAS_BASE_URL: string
  /** H5 端访问地址（CAS 登录后回调） */
  WEB_BASE_URL: string
}

const configs: Record<string, EnvConfig> = {
  development: {
    BASE_URL: '/api',
    CAS_BASE_URL: 'https://cas-qa.qdama.cn',
    WEB_BASE_URL: 'http://localhost:3000',
  },
  test: {
    BASE_URL: 'https://ebwmstest.qdama.cn:19011',
    CAS_BASE_URL: 'https://cas-qa.qdama.cn',
    WEB_BASE_URL: 'https://ebwmstest.qdama.cn',
  },
  production: {
    BASE_URL: 'https://ebwms.qdama.cn:19011',
    CAS_BASE_URL: 'https://cas.qdama.cn',
    WEB_BASE_URL: 'https://ebwms.qdama.cn',
  },
}

/** 当前环境配置 */
export const envConfig: EnvConfig = configs[ENV] || configs.test

/** 是否为本地开发环境 */
export const isLocalDev = ENV === 'development'

/**
 * 获取 CAS 登录后的回调地址（可选携带重定向路径）
 */
export function getServiceUrl(redirect?: string): string {
  if (isLocalDev) {
    const { protocol, hostname, port } = window.location
    const base = port ? `${protocol}//${hostname}:${port}` : `${protocol}//${hostname}`
    return redirect ? `${base}${redirect}` : `${base}/`
  }
  const base = envConfig.WEB_BASE_URL
  return redirect ? `${base}${redirect}` : `${base}/`
}

/**
 * 获取 CAS 登录页完整 URL
 * @param redirect 登录后跳转路径，如 /todo/list
 */
export function getCasLoginUrl(redirect?: string): string {
  return `${envConfig.CAS_BASE_URL}/login?service=${encodeURIComponent(getServiceUrl(redirect))}`
}
