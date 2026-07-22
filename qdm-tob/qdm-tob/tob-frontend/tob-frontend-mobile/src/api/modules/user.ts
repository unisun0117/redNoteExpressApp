/**
 * 客户认证模块 API（小程序端 /api/mall/auth）
 *
 * 对接后端 AuthController + WechatAuthController，统一返回 ApiResponse<T>。
 * 成功码 code === 0，token 存储 key 为 'token'（见 request.ts 拦截器与 user store）。
 */

import { http } from '@/api/request'
import type { ApiResponse } from '@/api/request'

// ---------------------------------------------------------------------------
// 公共类型（用别名避免嵌套泛型 >> 被编译器误判为右移运算符）
// ---------------------------------------------------------------------------

/** JWT Token 响应（登录/注册成功返回） */
export interface TokenResult {
  token: string
  expiresIn: number
  tokenType: string
}

type TokenResp = ApiResponse<TokenResult>
type NullResp = ApiResponse<null>

// ---------------------------------------------------------------------------
// 发送短信验证码：POST /api/mall/auth/sms/request?mobile={phone}
// ---------------------------------------------------------------------------

export function sendSmsCode(mobile: string): Promise<NullResp> {
  return http.post('/api/mall/auth/sms/request', {}, { params: { mobile } }) as Promise<NullResp>
}

// ---------------------------------------------------------------------------
// 注册：POST /api/mall/auth/register
// ---------------------------------------------------------------------------

export interface RegisterParams {
  /** 姓名（落库 real_name） */
  realName: string
  /** 手机号 */
  phone: string
  /** 短信验证码（SMS 跳过开关开启时可省略） */
  smsCode?: string
  /** 微信 openId（可选，微信环境内注册时传入并绑定） */
  wechatOpenId?: string
}

export function register(data: RegisterParams): Promise<TokenResp> {
  return http.post('/api/mall/auth/register', data) as Promise<TokenResp>
}

// ---------------------------------------------------------------------------
// 手机号 + 验证码登录：POST /api/mall/auth/sms/verify
// ---------------------------------------------------------------------------

export interface SmsLoginParams {
  phone: string
  /** 短信验证码（SMS 跳过开关开启时可省略） */
  smsCode?: string
}

export function loginBySms(data: SmsLoginParams): Promise<TokenResp> {
  return http.post('/api/mall/auth/sms/verify', data) as Promise<TokenResp>
}

// ---------------------------------------------------------------------------
// 微信一键登录：GET /api/mall/auth/wechat/login?phoneCode={phoneCode}&wxCode={wxCode}
// phoneCode = getPhoneNumber 返回的动态令牌（用于解密手机号）
// wxCode    = wx.login() 返回的临时 code（用于 code2Session 换取 openId）
// ---------------------------------------------------------------------------

export function wechatLogin(phoneCode: string, wxCode: string): Promise<TokenResp> {
  return http.get('/api/mall/auth/wechat/login', {
    params: { phoneCode, wxCode },
  }) as Promise<TokenResp>
}

// ---------------------------------------------------------------------------
// 退出登录：POST /api/mall/auth/logout（需 Authorization，拦截器自动注入）
// ---------------------------------------------------------------------------

export function logout(): Promise<NullResp> {
  return http.post('/api/mall/auth/logout') as Promise<NullResp>
}
