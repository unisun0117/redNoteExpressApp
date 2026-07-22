/**
 * 阿里云 SLS（日志服务）Web 端上报模块
 *
 * 提供 sendSlsLog 方法，用于上报前端错误、PV、接口异常等日志。
 * 使用 STS Token 鉴权方式，自动刷新临时凭证。
 */

import SlsTracker from '@aliyun-sls/web-track-browser'
import createStsPlugin from '@aliyun-sls/web-sts-plugin'

// ===== SLS 相关类型定义 =====

interface WebTrackerBrowserOptions {
  host: string
  project: string
  logstore: string
  time?: number
  count?: number
  topic?: string
  source?: string
  tags?: Record<string, unknown>
}

interface StsPlugin {
  transString: (obj: Record<string, unknown>) => Record<string, unknown>
  process: (
    url: string,
    payload: string,
    assignedDate?: number,
  ) => Promise<{ data: unknown; header: { [k: string]: string } }>
}

// ===== SLS 配置 =====

const opts: WebTrackerBrowserOptions = {
  host: 'cn-hangzhou.log.aliyuncs.com',
  project: 'erpappsls01-cn-hangzhou',
  logstore: 'wms-qas',
  time: 10,
  count: 10,
  topic: 'topic',
  source: 'source',
  tags: {
    tags: 'tags',
  },
}

// ===== STS Token 配置 =====

const stsOpt = {
  accessKeyId: '',
  accessKeySecret: '',
  securityToken: '',
  stsTokenFreshTime: undefined as unknown,
  refreshSTSTokenInterval: 3600000, // 默认 1 小时刷新
  refreshSTSToken: (): Promise<void> =>
    new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest()
      xhr.open('GET', 'https://ebwmstest.qdama.cn:19011/apply/api/common/sls/credential', true)
      xhr.send()
      xhr.onreadystatechange = () => {
        if (xhr.readyState === 4) {
          if (xhr.status === 200) {
            try {
              const credential = JSON.parse(xhr.response)
              if (credential?.data) {
                stsOpt.accessKeyId = credential.data.accessKeyId
                stsOpt.accessKeySecret = credential.data.accessKeySecret
                stsOpt.securityToken = credential.data.securityToken
                resolve()
              } else {
                reject(new Error('Wrong credential.'))
              }
            } catch {
              reject(new Error('Failed to parse credential response.'))
            }
          } else {
            reject(new Error('Wrong status code.'))
          }
        }
      }
    }),
}

// ===== Tracker 单例 =====

let tracker: SlsTracker | null = null
let stsPlugin: StsPlugin | null = null

function getTracker(): SlsTracker | null {
  // 本地开发环境不上报日志
  if (import.meta.env.VITE_SLS_ENABLED !== 'true') {
    return null
  }

  if (!tracker) {
    try {
      tracker = new SlsTracker(opts)
      stsPlugin = createStsPlugin(stsOpt) as unknown as StsPlugin
      tracker.useStsPlugin(stsPlugin as unknown as ReturnType<typeof createStsPlugin>)
    } catch (error) {
      console.warn('[SLS] Tracker 初始化失败:', error)
      return null
    }
  }
  return tracker
}

// ===== requestId 生成 =====

/**
 * 生成唯一请求 ID（UUID v4）
 * 优先使用 crypto.randomUUID()，降级为手动实现
 */
export function generateRequestId(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

// ===== 设备检测辅助函数 =====

function getDeviceTypeAndBrowser(): string {
  const ua = navigator.userAgent.toLowerCase()

  if (/iphone|ipad|ipod/.test(ua)) return 'ios'
  if (/android/.test(ua)) return 'android'

  // PC 端浏览器检测
  if (/edg/.test(ua)) return 'PC-Edge'
  if (/chrome/.test(ua) && !/edg/.test(ua)) return 'PC-谷歌浏览器'
  if (/firefox/.test(ua)) return 'PC-火狐浏览器'
  if (/safari/.test(ua) && !/chrome/.test(ua)) return 'PC-Safari'
  if (/trident|msie/.test(ua)) return 'PC-IE浏览器'

  return '未知设备'
}

function getUserId(): string {
  try {
    const userInfo = sessionStorage.getItem('userInfo')
    if (userInfo) {
      const parsed = JSON.parse(userInfo)
      return parsed.LoginID || parsed.LoginMobile || ''
    }
  } catch {
    // sessionStorage 数据解析异常，忽略
  }
  return ''
}

// ===== SLS 日志参数类型 =====

export interface SlsLogParams {
  system?: string
  deviceId?: string
  deviceType?: string
  errorResponseBody?: string
  requestId?: string
  requestTime?: string
  statusCode?: string | number
  storeCode?: string
  timeDifference?: string
  url?: string
  userId?: string
  version?: string
  terminalType?: string
  [key: string]: unknown
}

// ===== 发送 SLS 日志 =====

/** 获取 package.json 版本号（通过读取导入） */
let cachedVersion = '1.0.0'

export function setAppVersion(version: string): void {
  cachedVersion = version
}

export function sendSlsLog(params: SlsLogParams): void {
  try {
    const trackerInstance = getTracker()
    if (!trackerInstance) return

    const log: SlsLogParams = {
      deviceId: '',
      deviceType: getDeviceTypeAndBrowser(),
      errorResponseBody: params.errorResponseBody || '',
      requestId: params.requestId || '',
      requestTime: params.requestTime || '',
      statusCode: params.statusCode || '',
      storeCode: params.storeCode || '',
      system: params.system || 'tob-WEB',
      timeDifference: params.timeDifference || '',
      url: params.url || '',
      userId: params.userId || getUserId(),
      version: params.version || cachedVersion,
      terminalType: 'web',
      ...params,
    }

    trackerInstance.send(log)
  } catch (error) {
    // SLS 日志发送失败不应影响业务流程
    console.warn('[SLS] 日志发送失败:', error)
  }
}
