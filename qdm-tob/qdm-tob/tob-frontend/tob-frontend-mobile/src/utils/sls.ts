/**
 * 阿里云 SLS（Simple Log Service）日志监控
 *
 * 使用 STS 临时凭证进行日志上报，在接口请求失败时自动上传错误日志。
 *
 * 使用方式：
 *   import { sendSlsLog } from '@/utils/sls'
 *   sendSlsLog({ url: '/api/xxx', statusCode: '500', errorResponseBody: '...' })
 */

import SlsTracker from '@aliyun-sls/web-track-mini'
import createStsPlugin from '@aliyun-sls/web-sts-plugin'
import type { WebTrackerMiniOptions, StsPlugin } from '@aliyun-sls/web-types'

// ---------------------------------------------------------------------------
// 类型定义
// ---------------------------------------------------------------------------

/** SLS 日志字段 */
export interface SlsLogParams {
  /** 系统名称（必填，默认 'tob-小程序'） */
  system?: string
  /** 设备 ID */
  deviceId?: string
  /** 设备类型 */
  deviceType?: string
  /** 错误响应体 */
  errorResponseBody?: string
  /** 请求 ID */
  requestId?: string
  /** 请求时间 */
  requestTime?: string
  /** 状态码 */
  statusCode?: string
  /** 门店编码 */
  storeCode?: string
  /** 时间差 */
  timeDifference?: string
  /** 请求 URL */
  url?: string
  /** 用户 ID */
  userId?: string
  /** 版本号 */
  version?: string
  /** 其他自定义字段 */
  [key: string]: string | undefined
}

// ---------------------------------------------------------------------------
// 小程序信息
// ---------------------------------------------------------------------------

/** 获取小程序版本号（仅微信小程序） */
function getMiniProgramVersion(): string {
  try {
    // #ifdef MP-WEIXIN
    const accountInfo = wx.getAccountInfoSync()
    return accountInfo?.miniProgram?.version ?? ''
    // #endif
  } catch {
    // 非微信环境或 API 不可用时返回空字符串
  }
  return ''
}

const version = getMiniProgramVersion()

// ---------------------------------------------------------------------------
// SLS Tracker 配置
// ---------------------------------------------------------------------------

const opts: WebTrackerMiniOptions = {
  host: 'cn-hangzhou.log.aliyuncs.com',
  project: 'erpappsls01-cn-hangzhou',
  logstore: 'wms-qas',
  time: 10,      // 发送日志的时间间隔（秒）
  count: 10,     // 发送日志的条数阈值
  topic: 'topic',
  source: 'source',
  tags: {},
}

// ---------------------------------------------------------------------------
// STS 凭证配置
// ---------------------------------------------------------------------------

const stsOpt = {
  accessKeyId: '',
  accessKeySecret: '',
  securityToken: '',
  refreshSTSTokenInterval: 3600000, // 1 小时刷新一次
  stsTokenFreshTime: undefined as unknown,
  /**
   * 刷新 STS 临时凭证
   * 调用后端接口获取临时 AccessKey 信息
   */
  refreshSTSToken: (): Promise<void> =>
    new Promise((resolve, reject) => {
      wx.request({
        url: 'https://ebwmstest.qdama.cn:19011/apply/api/common/sls/credential',
        method: 'GET',
        success: (res) => {
          if (res.statusCode === 200) {
            const credential = res.data as Record<string, unknown>
            if (credential?.data) {
              const data = credential.data as Record<string, string>
              stsOpt.accessKeyId = data?.accessKeyId ?? ''
              stsOpt.accessKeySecret = data?.accessKeySecret ?? ''
              stsOpt.securityToken = data?.securityToken ?? ''
              resolve()
            } else {
              reject(new Error('Wrong credential: missing data field'))
            }
          } else {
            reject(new Error(`Wrong status code: ${res.statusCode}`))
          }
        },
        fail: (err) => {
          reject(err)
        },
      })
    }),
}

// ---------------------------------------------------------------------------
// Tracker 单例
// ---------------------------------------------------------------------------

let tracker: SlsTracker | null = null
let stsPlugin: StsPlugin | null = null

function getTracker(): SlsTracker {
  if (!tracker) {
    tracker = new SlsTracker(opts)
    stsPlugin = createStsPlugin(stsOpt)
    tracker.useStsPlugin(stsPlugin)
  }
  return tracker
}

// ---------------------------------------------------------------------------
// requestId 生成
// ---------------------------------------------------------------------------

/**
 * 生成唯一请求 ID（UUID v4）
 * 优先使用 crypto.randomUUID()，降级为手动实现
 */
export function generateRequestId(): string {
  try {
    // 小程序环境可能支持 crypto.randomUUID
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
      return crypto.randomUUID()
    }
  } catch {
    // crypto 不可用时降级
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

// ---------------------------------------------------------------------------
// 获取用户信息（从全局数据中的 userName 或 phoneNumber）
// ---------------------------------------------------------------------------

function getUserId(): string {
  try {
    // #ifdef MP-WEIXIN
    const app = getApp<Record<string, unknown>>()
    return (app?.globalData as Record<string, string> | undefined)?.name
      || (app?.globalData as Record<string, string> | undefined)?.phoneNumber
      || ''
    // #endif
  } catch {
    return ''
  }
  return ''
}

// ---------------------------------------------------------------------------
// 导出方法
// ---------------------------------------------------------------------------

/**
 * 发送日志到阿里云 SLS
 *
 * @param params - 日志字段对象，可覆盖默认字段
 * @param params.system - 系统名称，默认 `'tob-小程序'`
 *
 * @example
 * ```ts
 * sendSlsLog({
 *   url: '/api/user/login',
 *   statusCode: '500',
 *   errorResponseBody: '{"message":"Internal Server Error"}',
 * })
 * ```
 */
export function sendSlsLog(params: SlsLogParams): void {
  const tracker = getTracker()

  const log: Record<string, string> = {
    deviceId: params.deviceId || '',
    deviceType: params.deviceType || '小程序',
    errorResponseBody: params.errorResponseBody || '',
    requestId: params.requestId || '',
    requestTime: params.requestTime || '',
    statusCode: params.statusCode || '',
    storeCode: params.storeCode || '',
    system: params.system || 'tob-小程序',
    timeDifference: params.timeDifference || '',
    url: params.url || '',
    userId: params.userId || getUserId(),
    version: params.version || version,
    ...params,
  }

  tracker.send(log)
}
