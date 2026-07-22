/**
 * 隐私文档模块 API（对接后端 PrivacyDocMallController）
 *
 * 后端路径：/api/mall/privacy/docs
 * 成功码 code === 0，token 由 request.ts 拦截器自动注入
 */

import { http } from '@/api/request'
import type { ApiResponse } from '@/api/request'

// ---------------------------------------------------------------------------
// 类型定义（与后端 PrivacyDocSummaryVO / PrivacyDocViewVO 对齐）
// ---------------------------------------------------------------------------

/** 文档类型枚举（后端 DocType name()） */
export type DocType =
  | 'PRIVACY_POLICY'
  | 'PRIVACY_SUMMARY'
  | 'USER_RULES'
  | 'INFO_COLLECTION'
  | 'THIRD_PARTY_SHARING'
  | 'USER_AGREEMENT'

/** 文档状态枚举 */
export type DocStatus = 'UNPUBLISHED' | 'PUBLISHED' | 'WITHDRAWN'

/** 隐私文档列表项（不含富文本） */
export interface PrivacyDocSummary {
  id: number
  docType: DocType
  version: string
  h5Url: string
  fileUrl: string
  remark: string
  status: DocStatus
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
}

/** 隐私文档详情（含富文本） */
export interface PrivacyDocDetail {
  id: number
  docType: DocType
  version: string
  h5Url: string
  fileUrl: string
  remark: string
  richContent: string
  status: DocStatus
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
}

// ---------------------------------------------------------------------------
// API 方法
// ---------------------------------------------------------------------------

/**
 * 获取全部已发布文档列表（不含富文本）
 * GET /api/mall/privacy/docs/published
 */
export function getPublishedDocs(): Promise<ApiResponse<PrivacyDocSummary[]>> {
  return http.get('/api/mall/privacy/docs/published') as Promise<ApiResponse<PrivacyDocSummary[]>>
}

/**
 * 按类型获取当前生效文档详情（含富文本）
 * GET /api/mall/privacy/docs/type?docType=PRIVACY_POLICY
 */
export function getDocByType(docType: DocType): Promise<ApiResponse<PrivacyDocDetail>> {
  return http.get('/api/mall/privacy/docs/type', {
    params: { docType },
  }) as Promise<ApiResponse<PrivacyDocDetail>>
}

/**
 * 获取隐私政策历史版本列表
 * GET /api/mall/privacy/docs/privacy-policy/versions
 */
export function getPrivacyVersions(): Promise<ApiResponse<PrivacyDocSummary[]>> {
  return http.get('/api/mall/privacy/docs/privacy-policy/versions') as Promise<
    ApiResponse<PrivacyDocSummary[]>
  >
}

/**
 * 按版本号获取隐私政策详情（含富文本）
 * GET /api/mall/privacy/docs/privacy-policy/version?version=xxx
 */
export function getPrivacyByVersion(version: string): Promise<ApiResponse<PrivacyDocDetail>> {
  return http.get('/api/mall/privacy/docs/privacy-policy/version', {
    params: { version },
  }) as Promise<ApiResponse<PrivacyDocDetail>>
}

// ---------------------------------------------------------------------------
// 授权记录
// ---------------------------------------------------------------------------

/** 授权类型 */
export type AuthType = 'CAMERA' | 'ALBUM' | 'PRIVACY_POLICY' | 'PRIVACY_SUMMARY' | 'USER_RULES' | 'USER_AGREEMENT' | 'INFO_DOWNLOAD'

/**
 * 提交授权记录
 * POST /api/mall/privacy/auth
 */
export function submitAuthRecord(
  authType: AuthType,
  version?: string,
): Promise<ApiResponse<null>> {
  return http.post('/api/mall/privacy/auth', { authType, version }) as Promise<ApiResponse<null>>
}
