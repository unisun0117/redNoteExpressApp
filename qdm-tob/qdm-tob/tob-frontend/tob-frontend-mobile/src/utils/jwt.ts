/**
 * JWT 工具（仅用于解析本地 token 的展示字段，不做签名校验）
 *
 * 小程序端无 atob/TextDecoder，使用 uni.base64ToArrayBuffer 做跨端 base64 解码。
 * 仅解析 payload（第二段），用于读取后端写入的展示字段（如 nm 姓名、mbl 手机号）。
 */

/** JWT payload 的可读字段（与后端 TokenProvider 的 claim 约定对齐） */
export interface JwtPayload {
  /** 用户 ID */
  uid?: number | string
  /** 手机号 */
  mbl?: string
  /** 姓名（real_name / wechat_nickname） */
  nm?: string
  /** 认证类型 sms/wechat/cas/wecom */
  tp?: string
  /** token 唯一 ID */
  jti?: string
  /** 签发时间（秒） */
  iat?: number
  /** 过期时间（秒） */
  exp?: number
}

/**
 * 解析 JWT 的 payload 段。失败返回 null（调用方应兜底）。
 */
export function decodeJwtPayload(token: string): JwtPayload | null {
  if (!token || typeof token !== 'string') return null
  const parts = token.split('.')
  if (parts.length < 2) return null

  try {
    // base64url → base64
    let b64 = parts[1].replace(/-/g, '+').replace(/_/g, '/').replace(/\s/g, '')
    // 补齐 padding
    while (b64.length % 4) b64 += '='

    // 跨端 base64 → ArrayBuffer → UTF-8 字符串
    const buffer = uni.base64ToArrayBuffer(b64)
    const bytes = new Uint8Array(buffer)
    let binary = ''
    for (let i = 0; i < bytes.length; i++) {
      binary += String.fromCharCode(bytes[i])
    }
    const json = decodeURIComponent(escape(binary))

    return JSON.parse(json) as JwtPayload
  } catch {
    return null
  }
}

/**
 * 从 token 提取展示姓名（nm），失败返回空串。
 */
export function getNameFromToken(token: string): string {
  return decodeJwtPayload(token)?.nm ?? ''
}
