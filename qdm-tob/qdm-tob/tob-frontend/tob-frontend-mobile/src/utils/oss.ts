/**
 * 阿里云 OSS 直传模块（uni-app 小程序）
 *
 * 采用 STS 临时凭证模式：
 * 1. 调用后端 /api/mall/oss/sts-token?policy= 获取 STS 临时凭证
 * 2. 用 ali-oss SDK + STS 凭证直传 OSS
 *
 * 环境变量（预埋）：
 *   VITE_OSS_REGION / VITE_OSS_BUCKET / VITE_OSS_STS_ENDPOINT
 */
import OSS from 'ali-oss'

// ===== 环境变量 =====
const OSS_REGION = import.meta.env.VITE_OSS_REGION || 'oss-cn-hangzhou'
const OSS_BUCKET = import.meta.env.VITE_OSS_BUCKET || 'qdm-tob'
const OSS_STS_ENDPOINT = import.meta.env.VITE_OSS_STS_ENDPOINT || '/api/mall/oss/sts-token'

// ===== 类型 =====

export interface OssUploadOptions {
  /** 自定义目录（不填则用策略默认目录） */
  dir?: string
  /** 自定义文件名 */
  filename?: string
  /** 上传进度回调 */
  onProgress?: (percent: number) => void
  /** 最大重试次数，默认 3 */
  maxRetries?: number
  /** 上传策略名，默认 CUSTOMER_ARCHIVE */
  policy?: string
}

export interface OssUploadResult {
  url: string
  key: string
}

interface OssStsToken {
  accessKey: string
  secretKey: string
  securityToken: string
  expiration: string
  dir: string
  maxSize: number
}

// ===== STS 凭证缓存 =====

interface StsCacheEntry {
  token: OssStsToken
  expireAt: number
}

let stsCache: StsCacheEntry | null = null

/**
 * 从后端获取 STS 临时凭证
 */
async function fetchStsToken(policy: string): Promise<OssStsToken> {
  if (stsCache && Date.now() < stsCache.expireAt - 5 * 60 * 1000) {
    return stsCache.token
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: OSS_STS_ENDPOINT,
      method: 'GET',
      data: { policy },
      header: {
        'Authorization': 'Bearer ' + (uni.getStorageSync('token') || ''),
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const body = res.data as { code?: number; data?: OssStsToken }
          const token = body?.data || (res.data as unknown as OssStsToken)

          if (!token.accessKey || !token.secretKey || !token.securityToken) {
            reject(new Error('STS 凭证格式异常'))
            return
          }

          const expireMs = token.expiration
            ? new Date(token.expiration).getTime()
            : Date.now() + 3600 * 1000
          stsCache = { token, expireAt: expireMs }

          resolve(token)
        } else {
          reject(new Error(`获取 STS 凭证失败: HTTP ${res.statusCode}`))
        }
      },
      fail: (err) => reject(err),
    })
  })
}

// ===== 工具函数 =====

function uuid(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16)
  })
}

function getExt(filePath: string): string {
  const m = filePath.match(/\.([a-zA-Z0-9]+)$/)
  return m ? m[1] : 'png'
}

function delay(ms: number) {
  return new Promise((r) => setTimeout(r, ms))
}

// ===== 核心上传 =====

/**
 * 上传小程序临时文件到 OSS（STS 直传模式）
 *
 * @param tempFilePath 小程序临时文件路径
 * @param options 上传选项
 * @returns 上传结果（url + key）
 */
export async function ossUpload(
  tempFilePath: string,
  options?: OssUploadOptions,
): Promise<OssUploadResult> {
  const policy = options?.policy || 'CUSTOMER_ARCHIVE'

  // 1. 获取 STS 凭证
  const stsToken = await fetchStsToken(policy)

  // 2. 生成 OSS key
  const filename = options?.filename || `${uuid()}.${getExt(tempFilePath)}`
  const dir = options?.dir || stsToken.dir || 'other'
  const key = `${dir}/${filename}`

  // 3. 创建 OSS 客户端
  const client = new OSS({
    region: OSS_REGION,
    bucket: OSS_BUCKET,
    accessKeyId: stsToken.accessKey,
    accessKeySecret: stsToken.secretKey,
    stsToken: stsToken.securityToken,
  })

  // 4. 上传（带重试）
  const maxRetries = options?.maxRetries ?? 3
  let lastError: Error | null = null

  for (let attempt = 0; attempt < maxRetries; attempt++) {
    try {
      const result = await client.multipartUpload(key, tempFilePath, {
        partSize: 1024 * 1024,
        progress: (_p: number, checkpoint) => {
          if (checkpoint && options?.onProgress) {
            const percent = Math.round(
              ((checkpoint?.doneParts || 0) / (checkpoint?.partCount || 1)) * 100,
            )
            options.onProgress(percent)
          }
        },
      })
      return { url: result.url as string, key: result.name }
    } catch (err) {
      lastError = err as Error
      stsCache = null // 重试时清除缓存
      if (attempt < maxRetries - 1) {
        await delay(1000 * (attempt + 1))
      }
    }
  }

  throw new Error(`上传失败（已重试 ${maxRetries} 次）: ${lastError?.message}`)
}

/**
 * 快捷方法：上传图片
 */
export function ossUploadImage(
  tempFilePath: string,
  options?: OssUploadOptions,
): Promise<OssUploadResult> {
  return ossUpload(tempFilePath, options)
}
