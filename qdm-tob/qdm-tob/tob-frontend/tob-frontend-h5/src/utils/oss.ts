/**
 * 阿里云 OSS 直传模块（Web / H5）
 *
 * 采用 STS 临时凭证模式：前端从后端换取临时 AK/SK/Token → 直接上传到 OSS。
 * 环境变量（后期配置后生效，当前为预埋占位）：
 *   VITE_OSS_REGION        — OSS 区域，默认 oss-cn-hangzhou
 *   VITE_OSS_BUCKET        — OSS Bucket 名称
 *   VITE_OSS_DIR_IMAGE     — 图片上传目录，默认 images
 *   VITE_OSS_DIR_FILE      — 文件上传目录，默认 files
 *   VITE_OSS_STS_ENDPOINT  — STS 凭证接口路径，默认 /api/common/oss/sts-token
 */
import OSS from 'ali-oss'

// ===== 环境变量（预埋占位，后期配置后生效） =====

const OSS_REGION = import.meta.env.VITE_OSS_REGION || 'oss-cn-hangzhou'
const OSS_BUCKET = import.meta.env.VITE_OSS_BUCKET || ''
const OSS_DIR_IMAGE = import.meta.env.VITE_OSS_DIR_IMAGE || 'images'
const OSS_DIR_FILE = import.meta.env.VITE_OSS_DIR_FILE || 'files'
const OSS_STS_ENDPOINT = import.meta.env.VITE_OSS_STS_ENDPOINT || '/api/common/oss/sts-token'

// ===== 类型定义 =====

export interface OssUploadOptions {
  /** 子目录（如 'avatar'），默认按文件类型取 OSS_DIR_IMAGE / OSS_DIR_FILE */
  dir?: string
  /** 自定义文件名（不含目录），默认 UUID + 原扩展名 */
  filename?: string
  /** 进度回调 (0-100) */
  onProgress?: (percent: number) => void
  /** 最大重试次数，默认 3 */
  maxRetries?: number
}

export interface OssUploadResult {
  /** 文件访问 URL */
  url: string
  /** OSS 存储路径（key） */
  key: string
}

// ===== STS 凭证缓存 =====

interface StsCredentials {
  accessKeyId: string
  accessKeySecret: string
  stsToken: string
  expireAt: number // epoch ms
}

let stsCache: StsCredentials | null = null

/** 从后端获取 STS 临时凭证，带缓存（提前 5 分钟过期） */
async function getStsToken(): Promise<StsCredentials> {
  if (stsCache && Date.now() < stsCache.expireAt - 5 * 60 * 1000) {
    return stsCache
  }
  const resp = await fetch(OSS_STS_ENDPOINT)
  if (!resp.ok) throw new Error(`获取 STS 凭证失败: HTTP ${resp.status}`)
  const json = await resp.json()
  const data = json.data || json
  stsCache = {
    accessKeyId: data.accessKeyId || data.AccessKeyId,
    accessKeySecret: data.accessKeySecret || data.AccessKeySecret,
    stsToken: data.securityToken || data.SecurityToken,
    expireAt: Date.now() + ((data.expiration || 3600) * 1000),
  }
  return stsCache
}

// ===== OSS Client 单例 =====

let ossClient: OSS | null = null

async function getOssClient(): Promise<OSS> {
  if (!ossClient) {
    const sts = await getStsToken()
    ossClient = new OSS({
      region: OSS_REGION,
      bucket: OSS_BUCKET,
      accessKeyId: sts.accessKeyId,
      accessKeySecret: sts.accessKeySecret,
      stsToken: sts.stsToken,
      refreshSTSToken: async () => {
        const fresh = await getStsToken()
        return {
          accessKeyId: fresh.accessKeyId,
          accessKeySecret: fresh.accessKeySecret,
          stsToken: fresh.stsToken,
        }
      },
      refreshSTSTokenInterval: 300000,
    })
  }
  return ossClient
}

// ===== 工具函数 =====

/** 生成 OSS 存储路径 key */
function generateKey(file: File, options?: OssUploadOptions): string {
  const ext = file.name.split('.').pop() || 'bin'
  const filename = options?.filename || `${uuid()}.${ext}`
  const dir = options?.dir || (isImage(file) ? OSS_DIR_IMAGE : OSS_DIR_FILE)
  return `${dir}/${filename}`
}

function uuid(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

function isImage(file: File): boolean {
  return file.type.startsWith('image/')
}

/** 延迟 */
function delay(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

// ===== 核心上传方法 =====

/** 上传单个文件到 OSS */
export async function ossUpload(file: File, options?: OssUploadOptions): Promise<OssUploadResult> {
  const key = generateKey(file, options)
  const maxRetries = options?.maxRetries ?? 3

  let lastError: Error | null = null
  for (let attempt = 0; attempt < maxRetries; attempt++) {
    try {
      const client = await getOssClient()
      const result = await client.multipartUpload(key, file, {
        progress: (p: number, checkpoint) => {
          if (checkpoint && options?.onProgress) {
            const percent = Math.round(((checkpoint?.doneParts || 0) / (checkpoint?.partCount || 1)) * 100)
            options.onProgress(percent)
          }
        },
        partSize: 1024 * 1024, // 1MB 分片
      })
      return { url: result.url as string, key: result.name }
    } catch (err) {
      lastError = err as Error
      if (attempt < maxRetries - 1) {
        await delay(1000 * (attempt + 1))
        // 重置 client 以便下次重试获取新凭证
        ossClient = null
      }
    }
  }
  throw new Error(`上传失败（已重试 ${maxRetries} 次）: ${lastError?.message}`)
}

/** 上传图片（快捷方法） */
export function ossUploadImage(file: File, options?: OssUploadOptions): Promise<OssUploadResult> {
  return ossUpload(file, { ...options, dir: options?.dir || OSS_DIR_IMAGE })
}

/** 批量上传，支持整体进度回调 */
export async function ossUploadMultiple(
  files: File[],
  options?: OssUploadOptions & { onTotalProgress?: (percent: number) => void },
): Promise<OssUploadResult[]> {
  const results: OssUploadResult[] = []
  let completed = 0
  for (const file of files) {
    const result = await ossUpload(file, {
      ...options,
      onProgress: (p) => {
        options?.onTotalProgress?.(
          Math.round(((completed * 100) + p) / files.length),
        )
      },
    })
    results.push(result)
    completed++
  }
  return results
}
