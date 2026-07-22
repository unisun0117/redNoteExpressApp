/**
 * 阿里云 OSS 直传模块（Web / H5）
 *
 * 采用 STS 临时凭证模式：前端从后端换取临时 AK/SK/Token → 直接上传到 OSS。
 * 环境变量（后期配置后生效，当前为预埋占位）：
 *   VITE_OSS_REGION        — OSS 区域，默认 oss-cn-hangzhou
 *   VITE_OSS_BUCKET        — OSS Bucket 名称
 *   VITE_OSS_DIR_IMAGE     — 图片上传目录，默认 images
 *   VITE_OSS_DIR_FILE      — 文件上传目录，默认 files
 *   VITE_OSS_STS_POLICY    — STS 上传策略，默认 CUSTOMER_ARCHIVE
 */
import OSS from 'ali-oss'
import { getToken } from './auth'

// ===== 环境变量 =====

const OSS_REGION = import.meta.env.VITE_OSS_REGION || 'oss-cn-hangzhou'
const OSS_BUCKET = import.meta.env.VITE_OSS_BUCKET || ''
const OSS_DIR_IMAGE = import.meta.env.VITE_OSS_DIR_IMAGE || 'images'
const OSS_DIR_FILE = import.meta.env.VITE_OSS_DIR_FILE || 'files'
// ===== 类型定义 =====

/**
 * STS 策略类型（对应后端 StsPolicy 枚举名）。
 * 默认 CUSTOMER_ARCHIVE，可通过环境变量 VITE_OSS_STS_POLICY 全局覆盖，
 * 或通过 OssUploadOptions.policy 单次调用覆盖。
 */
export type StsPolicyType = 'AVATARS' | 'PRODUCTS' | 'CUSTOMER_ARCHIVE'

export interface OssUploadOptions {
  /** 子目录（如 'avatar'），默认按文件类型取 OSS_DIR_IMAGE / OSS_DIR_FILE */
  dir?: string
  /** 自定义文件名（不含目录），默认 UUID + 原扩展名 */
  filename?: string
  /** 进度回调 (0-100) */
  onProgress?: (percent: number) => void
  /** 最大重试次数，默认 3 */
  maxRetries?: number
  /** STS 上传策略，默认取 VITE_OSS_STS_POLICY 环境变量，兜底 CUSTOMER_ARCHIVE */
  policy?: StsPolicyType
}

export interface OssUploadResult {
  /** 文件访问 URL */
  url: string
  /** OSS 存储路径（key） */
  key: string
}

// ===== STS 凭证缓存（按策略隔离） =====

interface StsCredentials {
  accessKeyId: string
  accessKeySecret: string
  stsToken: string
  expireAt: number // epoch ms
}

const stsCacheMap = new Map<string, StsCredentials>()

/**
 * 从后端获取 STS 临时凭证，按策略缓存（提前 5 分钟过期）。
 * 通过 fetch + Authorization header（避免循环依赖 request.ts）。
 */
async function getStsToken(policy: StsPolicyType): Promise<StsCredentials> {
  const cached = stsCacheMap.get(policy)
  if (cached && Date.now() < cached.expireAt - 5 * 60 * 1000) {
    return cached
  }
  const token = getToken()
  const url = `/api/admin/oss/sts-token?policy=${policy}`
  const resp = await fetch(url, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  if (!resp.ok) throw new Error(`获取 STS 凭证失败: HTTP ${resp.status}`)
  const json = await resp.json()
  const data = json.data || json
  const cred = {
    accessKeyId: data.accessKey,
    accessKeySecret: data.secretKey,
    stsToken: data.securityToken,
    expireAt: Date.now() + (data.expiration ? new Date(data.expiration).getTime() - Date.now() : 3600 * 1000),
  }
  stsCacheMap.set(policy, cred)
  return cred
}

// ===== OSS Client（按策略 + region + bucket 缓存） =====

const ossClientMap = new Map<string, OSS>()

async function getOssClient(policy: StsPolicyType): Promise<OSS> {
  const key = `${OSS_REGION}:${OSS_BUCKET}:${policy}`
  if (!ossClientMap.has(key)) {
    const sts = await getStsToken(policy)
    const client = new OSS({
      region: OSS_REGION,
      bucket: OSS_BUCKET,
      accessKeyId: sts.accessKeyId,
      accessKeySecret: sts.accessKeySecret,
      stsToken: sts.stsToken,
      refreshSTSToken: async () => {
        const fresh = await getStsToken(policy)
        return {
          accessKeyId: fresh.accessKeyId,
          accessKeySecret: fresh.accessKeySecret,
          stsToken: fresh.stsToken,
        }
      },
      refreshSTSTokenInterval: 300000,
    })
    ossClientMap.set(key, client)
  }
  return ossClientMap.get(key)!
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
  const policy: StsPolicyType = options?.policy
    || (import.meta.env.VITE_OSS_STS_POLICY as StsPolicyType)
    || 'CUSTOMER_ARCHIVE'
  const key = generateKey(file, options)
  const maxRetries = options?.maxRetries ?? 3

  let lastError: Error | null = null
  const cacheKey = `${OSS_REGION}:${OSS_BUCKET}:${policy}`
  for (let attempt = 0; attempt < maxRetries; attempt++) {
    try {
      const client = await getOssClient(policy)
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
        // 重置对应策略的 client 以便下次重试获取新凭证
        ossClientMap.delete(cacheKey)
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
