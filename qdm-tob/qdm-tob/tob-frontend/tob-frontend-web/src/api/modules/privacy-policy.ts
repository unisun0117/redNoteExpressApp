import request from '@/utils/request'
import type {
  PrivacyDocSummaryVO,
  PrivacyDocViewVO,
  PrivacyDocCreationDTO,
  PrivacyDocEditDTO,
  AuthRecordVO,
  PageResult,
  DocTypeCode,
  DocTypeName,
  AuthTypeCode,
  AuthTypeName,
  DocStatusCode,
  DocStatusName,
} from '@/types/privacy-policy'

// ================================================================
// 通用响应包装（对应后端 ResponseResult）
// ================================================================

export interface ApiResponse<T> {
  code: number
  data: T
  msg: string
  traceId?: string
}

// ================================================================
// 查询参数
// ================================================================

/** 隐私文档分页查询参数（docType 传枚举名） */
export interface PrivacyDocQuery {
  docType?: DocTypeCode
  version?: string
  pageNum?: number
  pageSize?: number
}

/** 用户授权记录分页查询参数（authType 传枚举名） */
export interface AuthRecordQuery {
  openid?: string
  phone?: string
  authType?: AuthTypeCode
  pageNum?: number
  pageSize?: number
}

// ================================================================
// 枚举下拉选项（前端展示用，单一数据源）
// ================================================================

/** 文档类型下拉选项 */
export const DOC_TYPE_OPTIONS: { code: DocTypeCode; label: DocTypeName }[] = [
  { code: 'PRIVACY_POLICY', label: '隐私政策' },
  { code: 'PRIVACY_SUMMARY', label: '隐私政策摘要' },
  { code: 'USER_RULES', label: '用户管理规则及公约' },
  { code: 'INFO_COLLECTION', label: '个人信息收集清单' },
  { code: 'THIRD_PARTY_SHARING', label: '第三方共享个人信息清单' },
  { code: 'USER_AGREEMENT', label: '用户协议' },
]

/** 授权类型下拉选项 */
export const AUTH_TYPE_OPTIONS: { code: AuthTypeCode; label: AuthTypeName }[] = [
  { code: 'CAMERA', label: '相机' },
  { code: 'ALBUM', label: '相册' },
  { code: 'PRIVACY_POLICY', label: '隐私政策' },
  { code: 'PRIVACY_SUMMARY', label: '隐私政策摘要' },
  { code: 'USER_RULES', label: '用户管理规则与公约' },
  { code: 'USER_AGREEMENT', label: '用户协议' },
  { code: 'INFO_DOWNLOAD', label: '个人信息下载' },
]

/** 文档状态 → el-tag 颜色映射 */
export const DOC_STATUS_TAG: Record<DocStatusCode, 'success' | 'warning' | 'info'> = {
  UNPUBLISHED: 'warning',
  PUBLISHED: 'success',
  WITHDRAWN: 'info',
}

/** 文档状态枚举名 → 中文（兜底展示） */
export const DOC_STATUS_TEXT: Record<DocStatusCode, DocStatusName> = {
  UNPUBLISHED: '未发布',
  PUBLISHED: '已发布',
  WITHDRAWN: '已下架',
}

// ================================================================
// 隐私文档维护
// ================================================================

/** 隐私文档分页列表 */
export function getPrivacyDocPage(params: PrivacyDocQuery) {
  return request.get<unknown, ApiResponse<PageResult<PrivacyDocSummaryVO>>>(
    '/api/admin/privacy/docs/list',
    { params },
  )
}

/** 隐私文档详情 */
export function getPrivacyDoc(id: number) {
  return request.get<unknown, ApiResponse<PrivacyDocViewVO>>('/api/admin/privacy/docs/detail', {
    params: { id },
  })
}

/** 新增隐私文档（multipart：data=JSON, file=附件可选，返回新建 id） */
export function createPrivacyDoc(data: PrivacyDocCreationDTO, file?: File) {
  const formData = new FormData()
  formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }))
  if (file) {
    formData.append('file', file)
  }
  return request.post<unknown, ApiResponse<number>>('/api/admin/privacy/docs', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 编辑隐私文档（multipart：data=JSON, file=附件可选） */
export function updatePrivacyDoc(id: number, data: PrivacyDocEditDTO, file?: File) {
  const formData = new FormData()
  formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }))
  if (file) {
    formData.append('file', file)
  }
  return request.put<unknown, ApiResponse<null>>('/api/admin/privacy/docs', formData, {
    params: { id },
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 发布隐私文档 */
export function publishPrivacyDoc(id: number) {
  return request.put<unknown, ApiResponse<null>>('/api/admin/privacy/docs/publish', null, {
    params: { id },
  })
}

/** 下架隐私文档 */
export function withdrawPrivacyDoc(id: number) {
  return request.put<unknown, ApiResponse<null>>('/api/admin/privacy/docs/withdraw', null, {
    params: { id },
  })
}

/** 导出隐私文档（请求头 Action: export，返回二进制流） */
export function exportDocs(params: PrivacyDocQuery) {
  return request.get('/api/admin/privacy/docs/export', {
    params,
    responseType: 'blob',
    headers: { Action: 'export' },
  })
}

/** 下载文档附件（返回二进制流，非 ResponseResult 包装） */
export function downloadFile(id: number) {
  return request.get('/api/admin/privacy/docs/file', {
    params: { id },
    responseType: 'blob',
  })
}

// ================================================================
// 用户授权记录
// ================================================================

/** 用户授权记录分页列表 */
export function getAuthRecordPage(params: AuthRecordQuery) {
  return request.get<unknown, ApiResponse<PageResult<AuthRecordVO>>>(
    '/api/admin/privacy/auth-records/list',
    { params },
  )
}

/** 导出用户授权记录（请求头 Action: export，返回二进制流） */
export function exportAuthRecords(params: AuthRecordQuery) {
  return request.get('/api/admin/privacy/auth-records/export', {
    params,
    responseType: 'blob',
    headers: { Action: 'export' },
  })
}
