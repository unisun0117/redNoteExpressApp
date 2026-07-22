/**
 * HTTP 请求模块（基于 luch-request）
 *
 * 提供统一的 HTTP 客户端，支持：
 * - 多环境域名自动切换（通过 env.ts 配置）
 * - 全局请求拦截器（token 注入、loading 状态）
 * - 全局响应拦截器（错误处理、401 跳转登录）
 */

import Request from 'luch-request'
import { env } from '@/utils/env'
import { sendSlsLog, generateRequestId } from '@/utils/sls'

// ---------------------------------------------------------------------------
// 类型定义
// ---------------------------------------------------------------------------

/** 统一响应结构（与后端 ResponseResult 对齐：字段为 msg） */
export interface ApiResponse<T = unknown> {
  code: number
  data: T
  msg: string
  traceId?: string
}

/** 请求配置扩展 */
export interface RequestConfig {
  /** 是否显示 loading 提示，默认 false */
  showLoading?: boolean
  /** loading 提示文字，默认 "加载中..." */
  loadingText?: string
  /** 是否跳过 token 注入，默认 false */
  skipAuth?: boolean
}

// ---------------------------------------------------------------------------
// 创建 luch-request 实例
// ---------------------------------------------------------------------------

const http = new Request()

// 全局默认配置
http.setConfig((config) => {
  config.baseURL = env.apiBaseUrl
  config.timeout = 15000
  config.header = {
    'Content-Type': 'application/json',
  }
  return config
})

// ---------------------------------------------------------------------------
// 请求拦截器
// ---------------------------------------------------------------------------

let requestCount = 0

http.interceptors.request.use(
  (config) => {
    // --- 记录请求开始时间（用于 SLS 日志） ---
    ;(config as Record<string, unknown>).meta = { startTime: Date.now() }

    // --- Loading 状态 ---
    const custom = (config as unknown as Record<string, unknown>).custom as RequestConfig | undefined
    if (custom?.showLoading !== false) {
      if (requestCount === 0) {
        uni.showLoading({
          title: custom?.loadingText ?? '加载中...',
          mask: true,
        })
      }
      requestCount++
    }

    // --- Token 注入 ---
    if (!custom?.skipAuth) {
      // 从本地存储获取 token（避免 Pinia 循环依赖，小程序中 token 常持久化在 storage）
      const token = uni.getStorageSync('token')
      if (token) {
        config.header = {
          ...config.header,
          Authorization: `Bearer ${token}`,
        }
      }
    }

    // --- 注入前端生成的 requestId ---
    config.header = {
      ...config.header,
      requestId: generateRequestId(),
    }

    return config
  },
  (error) => {
    // 请求错误时也要关闭 loading
    if (requestCount > 0) {
      requestCount--
      if (requestCount === 0) {
        uni.hideLoading()
      }
    }
    return Promise.reject(error)
  },
)

// ---------------------------------------------------------------------------
// 响应拦截器
// ---------------------------------------------------------------------------

http.interceptors.response.use(
  (response) => {
    // --- 关闭 loading ---
    if (requestCount > 0) {
      requestCount--
      if (requestCount === 0) {
        uni.hideLoading()
      }
    }

    const res = response.data as ApiResponse

    // --- 业务状态码判断 ---
    // code === 0 或 code === 200 表示成功，直接返回
    console.log("请求res=====",res);
    if (res.code === 0 || res.code === 200) {
      return res as never
    }

    // 401 全局处理：token 过期跳登录页
    if (res.code === 401) {
      uni.removeStorageSync('token')
      uni.showToast({ title: '登录已过期，请重新登录', icon: 'none', duration: 2500 })
      setTimeout(() => uni.reLaunch({ url: '/pages/login/index' }), 1500)
      return Promise.reject({ code: 401, msg: '登录已过期' })
    }

    // 其他业务错误：透传给调用方，由业务代码根据 res.code / res.msg 自行展示
    return res as never
  },
  (error) => {
    // --- 关闭 loading ---
    if (requestCount > 0) {
      requestCount--
      if (requestCount === 0) {
        uni.hideLoading()
      }
    }

    // --- SLS 错误日志上报 ---
    const meta = (error?.config as Record<string, unknown>)?.meta as { startTime?: number } | undefined
    const requestTime = meta?.startTime ? `${Date.now() - meta.startTime}ms` : ''

    sendSlsLog({
      url: error?.config?.url ?? '',
      statusCode: String(error?.statusCode ?? '0'),
      errorResponseBody: error?.data ? JSON.stringify(error.data) : error?.errMsg ?? '',
      requestTime,
    })

    // --- 优先提取后端返回的业务错误信息 ---
    const bizMsg: string | undefined = error?.data?.msg
    const message = bizMsg || '网络异常，请检查网络连接'

    if (error?.statusCode === 401 || error?.statusCode === 403) {
      uni.removeStorageSync('token')
      setTimeout(() => uni.reLaunch({ url: '/pages/login/index' }), 1500)
    }

    uni.showToast({ title: message, icon: 'none', duration: 3000 })
    return Promise.reject(error)
  },
)

// ---------------------------------------------------------------------------
// 导出实例
// ---------------------------------------------------------------------------

export { http }
export default http
