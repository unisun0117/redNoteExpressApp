import request from '@/utils/request'
import { getServiceUrl } from '@/config/env'
import { setToken } from '@/utils/auth'

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string
  real_name: string
  mobile: string
}

/** 后端 TokenResponseVO 格式 */
interface TokenRawResponse {
  token?: string
  tokenType?: string
  expiresIn?: number
}

/** 后端 CurrentUserVO 格式 */
interface CurrentUserRaw {
  name?: string
  phone?: string
}

export const loginByCasTicket = loginByTicket


/**
 * 通过 CAS Ticket 换取 Token
 * 本地开发 → POST /api/admin/auth/cas/login（对接本机后端）
 */
export async function loginByTicket(ticket: string, url: string = ''): Promise<LoginResponse> {
  // 1. 用 CAS ticket 换取 JWT token
  const casResp = await request.post('/api/admin/auth/cas/login', null, {
    params: { ticket, service: url || getServiceUrl() },
  }) as unknown as { code: number; data: TokenRawResponse }

  if (casResp.code !== 0 || !casResp.data?.token) {
    throw new Error('CAS 登录失败')
  }
  const token = casResp.data.token

  // 2. 先保存 token
  setToken(token)

  // 3. 用户信息（/current-user 暂不可用，后续修复后启用；不阻塞登录）
  return { token, real_name: '', mobile: '' }
}

/**
 * 通过用户名密码登录（本地开发环境）
 * GET /oauth2/unifty/token?grant_type=password&username=xxx&password=xxx&...
 */
interface OAuthResponse {
  access_token?: string
  real_name?: string
  mobile?: string
  data?: { access_token?: string; real_name?: string; mobile?: string }
}

export async function loginByPassword(username: string, password: string): Promise<LoginResponse> {
  const raw = await request.get('/oauth2/unifty/token', {
    params: { client_id: 'default', client_secret: 'default_sec', grant_type: 'password', username, password, scope: 'all' },
  }) as unknown as OAuthResponse

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const payload = (raw.data?.access_token ? raw.data : raw) as any
  if (!payload.access_token) throw new Error('Token 获取失败')

  return { token: payload.access_token, real_name: payload.real_name || '', mobile: payload.mobile || '' }
}

/**
 * 获取当前登录用户信息
 */
export interface CurrentUserInfo {
  name: string
  phone: string
}

export async function getCurrentUser(): Promise<CurrentUserInfo> {
  const res = await request.get<{ code: number; data: CurrentUserInfo }>('/api/admin/auth/current-user')
  return (res as unknown as { code: number; data: CurrentUserInfo }).data
}
