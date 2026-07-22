/**
 * 企业微信 JS-SDK 核心鉴权逻辑
 *
 * 职责：
 * 1. 统一管理 wx.config / wx.agentConfig 鉴权流程
 * 2. 提供 WWOpenData 初始化入口
 * 3. 兜底非企业微信环境降级
 */

import { get } from './axios'

// ===== 常量 =====

/** 常用 JS-SDK API 列表 */
export const WX_DEFAULT_API_LIST = [
  'hideOptionMenu',
  'showOptionMenu',
  'closeWindow',
  'hideMenuItems',
  'showMenuItems',
  'getNetworkType',
  'getLocation',
  'previewImage',
  'scanQRCode',
  'openDefaultBrowser',
  'onMenuShareTimeline',
  'onMenuShareAppMessage',
  'chooseImage',
  'uploadImage',
  'downloadImage',
]

// ===== 环境判断 =====

/** 是否在企业微信客户端内 */
export function isWxWork(): boolean {
  const ua = navigator.userAgent.toLowerCase()
  return /wxwork/i.test(ua)
}

/** 是否在微信客户端内 */
export function isWechat(): boolean {
  const ua = navigator.userAgent.toLowerCase()
  return /micromessenger/i.test(ua) && !/wxwork/i.test(ua)
}

// ===== 鉴权签名请求 =====

interface WxSignatureResult {
  appId: string
  nonceStr: string
  timestamp: number
  signature: string
}

/**
 * 从后端获取 wx.config 鉴权签名
 *
 * @param url 当前页面 URL（签名用），默认取 location.href
 */
export async function fetchWxConfigSignature(url?: string): Promise<WxSignatureResult> {
  const targetUrl = url || window.location.href
  const res = await get<WxSignatureResult>('/apply/api/common/wx/jsapi-signature', {
    url: targetUrl,
  })
  return res.data
}

/**
 * 从后端获取 wx.agentConfig 鉴权签名
 *
 * @param url 当前页面 URL（签名用），默认取 location.href
 */
export async function fetchWxAgentSignature(url?: string): Promise<WxSignatureResult> {
  const targetUrl = url || window.location.href
  const res = await get<WxSignatureResult>('/apply/api/common/wx/agent-signature', {
    url: targetUrl,
  })
  return res.data
}

// ===== 核心鉴权初始化 =====

interface WxInitOptions {
  /** JS 接口列表，默认 WX_DEFAULT_API_LIST */
  jsApiList?: string[]
  /** 是否开启 debug 模式 */
  debug?: boolean
  /** 当前页面 URL（签名用） */
  url?: string
}

/**
 * 企业微信 JS-SDK 鉴权初始化
 *
 * 调用 wx.config + wx.ready + wx.error，返回 Promise 表示鉴权结果。
 * 若不在企微环境下则静默跳过（resolve false）。
 *
 * @example
 *   await initWxConfig({ debug: import.meta.env.DEV })
 *   // 鉴权通过后即可安全调用 wx.xxx API
 */
export async function initWxConfig(opts: WxInitOptions = {}): Promise<boolean> {
  if (!isWxWork()) {
    console.log('[WX] 非企业微信环境，跳过 wx.config 初始化')
    return false
  }

  const { jsApiList = WX_DEFAULT_API_LIST, debug = import.meta.env.DEV, url } = opts

  try {
    const sign = await fetchWxConfigSignature(url)

    return new Promise<boolean>((resolve) => {
      wx.config({
        debug,
        appId: sign.appId,
        timestamp: sign.timestamp,
        nonceStr: sign.nonceStr,
        signature: sign.signature,
        jsApiList,
      })

      wx.ready(() => {
        console.log('[WX] wx.config 鉴权成功')
        resolve(true)
      })

      wx.error((err: { errMsg: string }) => {
        console.error('[WX] wx.config 鉴权失败:', err.errMsg)
        resolve(false)
      })
    })
  } catch (error) {
    console.error('[WX] 获取鉴权签名失败:', error)
    return false
  }
}

/**
 * 企业微信 agentConfig 鉴权初始化
 *
 * 用于企业微信应用的页面（需要进一步注入应用身份）。
 * 调用前须确保 wx.config 已完成。
 *
 * @example
 *   await initWxConfig()
 *   await initWxAgentConfig()
 */
export async function initWxAgentConfig(opts: WxInitOptions = {}): Promise<boolean> {
  if (!isWxWork()) {
    console.log('[WX] 非企业微信环境，跳过 agentConfig 初始化')
    return false
  }

  const { jsApiList = WX_DEFAULT_API_LIST, url } = opts

  try {
    const sign = await fetchWxAgentSignature(url)

    return new Promise<boolean>((resolve) => {
      wx.agentConfig({
        corpid: sign.appId,
        agentid: import.meta.env.VITE_WX_AGENT_ID || '',
        timestamp: sign.timestamp,
        nonceStr: sign.nonceStr,
        signature: sign.signature,
        jsApiList,
        success: () => {
          console.log('[WX] agentConfig 鉴权成功')
          resolve(true)
        },
        fail: (err) => {
          console.error('[WX] agentConfig 鉴权失败:', err.errMsg)
          resolve(false)
        },
      })
    })
  } catch (error) {
    console.error('[WX] 获取 agentConfig 签名失败:', error)
    return false
  }
}

// ===== WWOpenData 初始化 =====

/**
 * 初始化企业微信开放数据组件
 *
 * 在页面中需要获取用户敏感信息时调用。
 * 需配合后台解密逻辑使用。
 */
export function initWWOpenData(): void {
  if (!isWxWork()) {
    console.log('[WX] 非企业微信环境，跳过 WWOpenData 初始化')
    return
  }

  console.log('[WX] WWOpenData 初始化完成（依赖 wx.config 鉴权）')
  // WWOpenData 在 wx.config 成功后即可使用，无需额外初始化
  // 具体使用时调用：wx.invoke('ww.getOpenData', { ... }, callback)
}

// ===== 通用助手 =====

/**
 * 在企业微信环境下安全调用 API，非企微环境静默忽略
 *
 * @example
 *   safeInvoke('hideOptionMenu')
 */
export function safeInvoke(
  action: () => void,
  checkWx = true,
): void {
  if (checkWx && !isWxWork()) {
    return
  }
  try {
    action()
  } catch (error) {
    console.error('[WX] API 调用异常:', error)
  }
}

/**
 * 隐藏页面基础菜单
 */
export function hideBasicMenu(): void {
  safeInvoke(() => {
    wx.hideOptionMenu()
  })
}
