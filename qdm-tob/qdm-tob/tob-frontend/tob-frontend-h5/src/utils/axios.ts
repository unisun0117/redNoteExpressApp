import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'
import { showToast } from 'vant'
import { generateRequestId } from './sls'
import { getCasLoginUrl } from '@/config/env'

// ===== 核心类型定义 =====

/** 后端统一响应结构 */
export interface ApiResponse<T = unknown> {
  code: number
  data: T
  msg: string
  success: boolean
}

/** 请求额外配置 */
interface RequestExtraConfig {
  /** 是否跳过自动错误提示 */
  skipErrorToast?: boolean
  /** 是否跳过静默令牌刷新（如登录接口自身） */
  skipTokenRefresh?: boolean
}

/** 合并后的完整请求配置 */
type FullRequestConfig = InternalAxiosRequestConfig & RequestExtraConfig

// ===== 环境域名切换 =====

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

// ===== 创建 Axios 实例 =====

const instance: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// ===== 请求拦截器 =====

instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 注入 Token
    const token = sessionStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 注入企业微信 CorpID / AgentID 到请求头（按需）
    const wxCorpId = import.meta.env.VITE_WX_CORP_ID
    const wxAgentId = import.meta.env.VITE_WX_AGENT_ID
    if (wxCorpId && config.headers) {
      config.headers['X-WX-Corp-ID'] = wxCorpId
    }
    if (wxAgentId && config.headers) {
      config.headers['X-WX-Agent-ID'] = wxAgentId
    }

    // 注入前端生成的 requestId
    if (config.headers) {
      config.headers['requestId'] = generateRequestId()
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// ===== 响应拦截器 =====

instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { data, config } = response
    const fullConfig = config as FullRequestConfig

    // 业务状态码判断
    if (data.code !== 0 && data.code !== 200) {
      if (!fullConfig.skipErrorToast) {
        showToast(data.msg || '请求失败，请稍后重试')
      }

      // 401 → 跳 CAS 统一登录
      if (data.code === 401) {
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('userInfo')
        if (window.location.pathname !== '/login') {
          window.location.replace(getCasLoginUrl())
        }
      }

      // 403 → 无 token 时跳登录页，有 token 时仅 toast（已在上面 showToast）
      if (data.code === 403) {
        const hasToken = !!sessionStorage.getItem('token')
        if (!hasToken) {
          sessionStorage.setItem('login_error', data.msg || '暂无系统权限')
          sessionStorage.removeItem('token')
          sessionStorage.removeItem('userInfo')
          if (window.location.pathname !== '/login') {
            window.location.replace('/login')
          }
        }
      }

      return Promise.reject(new Error(data.msg || 'Request Failed'))
    }

    return response
  },
  (error) => {
    // HTTP 状态码异常
    const config = error.config as FullRequestConfig | undefined

    if (error.response) {
      const { status } = error.response
      const responseData = error.response.data as { message?: string; msg?: string } | undefined
      const serverMsg = responseData?.msg || responseData?.message || ''

      const defaultMsg: Record<number, string> = {
        400: '请求参数错误',
        401: '登录已过期，请重新登录',
        403: '暂无访问权限',
        404: '请求的资源不存在',
        500: '服务器异常，请稍后重试',
        502: '网关异常',
      }

      if (status === 401) {
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('userInfo')
        if (window.location.pathname !== '/login') {
          window.location.replace(getCasLoginUrl())
        }
      }
      if (status === 403) {
        // 无 token 时是 CAS 登录流报错，跳登录页展示；有 token 时仅 toast
        const hasToken = !!sessionStorage.getItem('token')
        if (!hasToken) {
          sessionStorage.setItem('login_error', serverMsg || '暂无系统权限')
          sessionStorage.removeItem('token')
          sessionStorage.removeItem('userInfo')
          if (window.location.pathname !== '/login') {
            window.location.replace('/login')
          }
        } else {
          showToast(serverMsg || '暂无系统权限')
        }
      }

      const msg = serverMsg || defaultMsg[status] || '网络异常，请稍后重试'

      if (!config?.skipErrorToast) {
        showToast(msg)
      }
    } else if (error.code === 'ECONNABORTED') {
      if (!config?.skipErrorToast) {
        showToast('请求超时，请检查网络')
      }
    } else {
      if (!config?.skipErrorToast) {
        showToast('网络连接失败，请检查网络')
      }
    }

    return Promise.reject(error)
  },
)

// ===== 封装便捷请求方法 =====

export function get<T = unknown>(
  url: string,
  params?: Record<string, unknown>,
  config?: RequestExtraConfig,
): Promise<ApiResponse<T>> {
  return instance.get(url, { params, ...config } as AxiosRequestConfig)
}

export function post<T = unknown>(
  url: string,
  data?: Record<string, unknown>,
  config?: RequestExtraConfig,
): Promise<ApiResponse<T>> {
  return instance.post(url, data, config as AxiosRequestConfig)
}

export function put<T = unknown>(
  url: string,
  data?: Record<string, unknown>,
  config?: RequestExtraConfig,
): Promise<ApiResponse<T>> {
  return instance.put(url, data, config as AxiosRequestConfig)
}

export function del<T = unknown>(
  url: string,
  params?: Record<string, unknown>,
  config?: RequestExtraConfig,
): Promise<ApiResponse<T>> {
  return instance.delete(url, { params, ...config } as AxiosRequestConfig)
}

export default instance
