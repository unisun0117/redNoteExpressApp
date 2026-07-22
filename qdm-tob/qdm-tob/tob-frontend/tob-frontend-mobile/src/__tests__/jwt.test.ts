/**
 * utils/jwt 单元测试
 *
 * 覆盖登录后「从 token 取 nm 姓名」的逻辑——这是修复
 * 「微信/短信登录后首页显示空名字」的前端侧关键路径。
 * 后端 provider 传 null name 时 nm 缺失，getNameFromToken 应安全返回空串。
 *
 * 本测试不依赖 DOM（仅用 atob / ArrayBuffer / uni mock），跑在 node 环境
 * 以绕开 jsdom 29 + Node 18 的 ERR_REQUIRE_ESM。
 */

// @vitest-environment node

import { beforeEach, describe, it, expect, vi } from 'vitest'
import { decodeJwtPayload, getNameFromToken } from '@/utils/jwt'

// setup.ts 未 mock uni.base64ToArrayBuffer，这里用 jsdom 自带的 atob 实现等价解码，
// 使 utils/jwt.ts 的跨端解码路径在测试环境下可用。
beforeEach(() => {
  const uniObj = uni as unknown as Record<string, (...args: unknown[]) => unknown>
  uniObj.base64ToArrayBuffer = vi.fn((b64: string) => {
    const bin = atob(b64)
    const buf = new ArrayBuffer(bin.length)
    const view = new Uint8Array(buf)
    for (let i = 0; i < bin.length; i++) view[i] = bin.charCodeAt(i)
    return buf
  })
})

describe('getNameFromToken', () => {
  // 后端真实签发的 token：payload nm="TestUser"
  const asciiToken =
    'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ1c2VyOjEiLCJ1aWQiOjEsInRwIjoic21zIiwibWJsIjoiMTM4MDAwMDAwMDEiLCJubSI6IlRlc3RVc2VyIiwiYXV0IjpbXSwianRpIjoiNTBlZGM3MzctOWE5OS00OTM3LWFmM2UtMThhN2YxOWE0ZjIxIiwiaWF0IjoxNzgyOTc0MDEyLCJleHAiOjE3ODI5Nzc2MTJ9.JhtAfgBF4dnbUKawG6l3e2mtQ-ZV8lNkTX6QrldBeDkCs72OUEnfLjsFTmHg4Z9b'

  // payload nm="李大琪"（验证 UTF-8 多字节解码）
  const chineseToken =
    'eyJhbGciOiJIUzM4NCJ9.eyJ1aWQiOjMsInRwIjoic21zIiwibWJsIjoiMTM1MzIzMDE1NjUiLCJubSI6IuadjuWkp-eQqiIsImF1dCI6W10sImp0aSI6InRlc3QiLCJpYXQiOjE3ODI5NzQwMTIsImV4cCI6MTc4Mjk3NzYxMn0.sig'

  // payload 无 nm 字段（对应后端 provider 传 null name 的场景）
  const noNmToken =
    'eyJhbGciOiJIUzM4NCJ9.eyJ1aWQiOjMsInRwIjoic21zIiwibWJsIjoiMTM1MzIzMDE1NjUifQ.sig'

  it('从 token 提取 ASCII 姓名', () => {
    expect(getNameFromToken(asciiToken)).toBe('TestUser')
  })

  it('从 token 提取中文姓名（UTF-8 解码）', () => {
    expect(getNameFromToken(chineseToken)).toBe('李大琪')
  })

  it('token 无 nm 字段时返回空串', () => {
    // 后端 SmsAuthenticationProvider/WechatAuthenticationProvider 曾传 null，
    // 此时前端不应抛错，而是返回空串让首页兜底「客户」
    expect(getNameFromToken(noNmToken)).toBe('')
  })

  it('格式错误的 token 返回空串', () => {
    expect(getNameFromToken('not-a-jwt')).toBe('')
    expect(getNameFromToken('')).toBe('')
    expect(getNameFromToken('a.b')).toBe('')
  })
})

describe('decodeJwtPayload', () => {
  const chineseToken =
    'eyJhbGciOiJIUzM4NCJ9.eyJ1aWQiOjMsInRwIjoic21zIiwibWJsIjoiMTM1MzIzMDE1NjUiLCJubSI6IuadjuWkp-eQqiIsImF1dCI6W10sImp0aSI6InRlc3QiLCJpYXQiOjE3ODI5NzQwMTIsImV4cCI6MTc4Mjk3NzYxMn0.sig'

  it('解析完整 payload 字段', () => {
    const p = decodeJwtPayload(chineseToken)
    expect(p?.uid).toBe(3)
    expect(p?.tp).toBe('sms')
    expect(p?.mbl).toBe('13532301565')
    expect(p?.nm).toBe('李大琪')
  })

  it('格式错误返回 null', () => {
    expect(decodeJwtPayload('bad')).toBeNull()
    expect(decodeJwtPayload('')).toBeNull()
  })
})
