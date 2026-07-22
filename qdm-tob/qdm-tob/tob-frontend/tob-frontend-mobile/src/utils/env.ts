/**
 * 环境配置工具
 *
 * 根据 Vite 构建模式自动切换对应的环境变量
 * 打包时通过 `--mode` 参数指定环境：
 *   - development: 本地开发 (默认)
 *   - test:        测试环境
 *   - production:  生产环境
 */

/** 当前环境模式 */
export type EnvMode = 'development' | 'test' | 'production'

/** 环境配置类型 */
export interface EnvConfig {
  /** API 请求基础域名 */
  apiBaseUrl: string
  /** H5 前端基础域名 */
  h5BaseUrl: string
  /** 当前环境模式 */
  mode: EnvMode
  /** 是否为开发环境 */
  isDevelopment: boolean
  /** 是否为测试环境 */
  isTest: boolean
  /** 是否为生产环境 */
  isProduction: boolean
}

/**
 * 获取当前运行环境模式
 * 从 Vite 注入的 import.meta.env 读取
 */
function getEnvMode(): EnvMode {
  const mode = (import.meta as Record<string, unknown>).env.VITE_APP_ENV as string | undefined

  if (mode === 'test') return 'test'
  if (mode === 'production') return 'production'
  return 'development'
}

const mode = getEnvMode()

function getH5BaseUrl(): string {
  const cfg = import.meta as Record<string, unknown>
  const envVal = cfg.env.VITE_H5_BASE_URL as string | undefined
  if (envVal) return envVal
  // fallback: derive from API base — H5 is on same domain without port
  const apiBase = (cfg.env.VITE_API_BASE_URL as string) ?? ''
  if (apiBase) {
    try {
      const url = new URL(apiBase)
      url.port = ''
      url.pathname = ''
      return url.toString().replace(/\/$/, '')
    } catch {
      return ''
    }
  }
  return ''
}

/** 环境配置对象（只读） */
export const env: Readonly<EnvConfig> = {
  apiBaseUrl: (import.meta as Record<string, unknown>).env.VITE_API_BASE_URL as string ?? '',
  h5BaseUrl: getH5BaseUrl(),
  mode,
  isDevelopment: mode === 'development',
  isTest: mode === 'test',
  isProduction: mode === 'production',
}
