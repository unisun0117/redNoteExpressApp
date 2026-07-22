import axios, { type AxiosInstance, type InternalAxiosRequestConfig, AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from './auth'
import { getCasLoginUrl } from '@/config/env'
import { sendSlsLog, generateRequestId } from './sls'

const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// ===== 请求拦截器 =====
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 注入前端生成的 requestId
    if (config.headers) {
      config.headers['requestId'] = generateRequestId()
    }
    // 记录请求开始时间（挂载到 config 扩展属性）
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    ;(config as any)._requestStartTime = Date.now()
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  },
)

// ===== 计算请求耗时 =====
function getRequestDuration(config: InternalAxiosRequestConfig): string {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const startTime = (config as any)._requestStartTime as number | undefined
  if (startTime) {
    return `${Date.now() - startTime}ms`
  }
  return ''
}

// ===== 构建 SLS 日志并上报 =====
function logErrorToSls(
  config: InternalAxiosRequestConfig,
  statusCode: number | string,
  errorBody: string,
) {
  const fullUrl = config.baseURL ? `${config.baseURL}${config.url}` : config.url || ''

  sendSlsLog({
    url: fullUrl,
    statusCode: String(statusCode),
    errorResponseBody: errorBody,
    requestTime: getRequestDuration(config),
    timeDifference: new Date().toISOString(),
    requestId: config.headers?.['requestId'] as string | undefined,
  })
}

// ===== 响应拦截器 =====
instance.interceptors.response.use(
  (response) => {
    const { data } = response

    // 业务状态码非 0/200 时统一提示错误并上报 SLS
    if (data.code !== undefined && data.code != 0 && data.code != '200') {
      const message = data.message || data.msg || '请求失败'
      ElMessage.error(message)
      logErrorToSls(response.config, data.code, JSON.stringify(data))
      return Promise.reject(new Error(message))
    }

    return data
  },
  (error: AxiosError) => {
    const config = error.config as InternalAxiosRequestConfig

    // 网络错误处理
    if (!error.response) {
      ElMessage.error('网络异常，请检查网络连接')
      logErrorToSls(config, 0, error.message || 'Network Error')
      return Promise.reject(error)
    }

    const { status, data } = error.response
    const errorBody = typeof data === 'string' ? data : JSON.stringify(data)
    const serverMsg =
      (data as { message?: string; msg?: string })?.msg ||
      (data as { message?: string; msg?: string })?.message

    // 上报 SLS 日志
    logErrorToSls(config, status, errorBody)

    // 401：清除 token 并跳转 CAS 统一登录
    if (status === 401) {
      clearAuth()
      const currentPath = window.location.pathname
      if (currentPath !== '/login') {
        ElMessage.error(serverMsg || '登录已过期，请重新登录')
        window.location.replace(getCasLoginUrl(currentPath))
      }
      return Promise.reject(error)
    }

    // 403：仅提示无权限，不跳 CAS
    if (status === 403) {
      ElMessage.error('没有操作权限，请联系管理员')
      return Promise.reject(error)
    }

    // 优先使用后端返回的错误消息，其次用默认提示
    const defaultMsg: Record<number, string> = {
      400: '请求参数错误',
      403: '没有访问权限',
      404: '请求的资源不存在',
      500: '服务器内部错误',
      502: '网关错误',
      503: '服务暂不可用',
    }
    ElMessage.error(serverMsg || defaultMsg[status] || `请求失败 (${status})`)

    return Promise.reject(error)
  },
)

export default instance
