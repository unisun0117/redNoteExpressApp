import { post } from '@/utils/axios'
import { getServiceUrl } from '@/config/env'

/**
 * 登录响应
 */
export interface LoginResponse {
  access_token: string
  real_name: string
  mobile: string
}

/**
 * 通过 CAS Ticket 换取 Token
 * POST /api/admin/auth/cas/login
 */
export async function loginByTicket(ticket: string): Promise<LoginResponse> {
  const res = await post<{
    code: number
    data: { access_token: string; real_name: string; mobile: string; token: string }
  }>('/api/admin/auth/cas/login', {
    ticket,
    service: getServiceUrl(),
  } as unknown as Record<string, unknown>)

  const payload = res.data.data
  const token = payload.access_token || payload.token
  if (!token) {
    throw new Error('Token 获取失败：响应中缺少 access_token')
  }

  return {
    access_token: token,
    real_name: payload.real_name || '',
    mobile: payload.mobile || '',
  }
}
