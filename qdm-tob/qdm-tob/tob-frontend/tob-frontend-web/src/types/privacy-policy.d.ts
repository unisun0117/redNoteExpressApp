// ================================================================
// 枚举名（后端返回的英文 enum NAME）
// ================================================================

/** 文档状态枚举名 */
export type DocStatusCode = 'UNPUBLISHED' | 'PUBLISHED' | 'WITHDRAWN'

/** 文档类型枚举名 */
export type DocTypeCode =
  | 'PRIVACY_POLICY'
  | 'PRIVACY_SUMMARY'
  | 'USER_RULES'
  | 'INFO_COLLECTION'
  | 'THIRD_PARTY_SHARING'
  | 'USER_AGREEMENT'

/** 授权类型枚举名 */
export type AuthTypeCode =
  | 'CAMERA'
  | 'ALBUM'
  | 'PRIVACY_POLICY'
  | 'PRIVACY_SUMMARY'
  | 'USER_RULES'
  | 'USER_AGREEMENT'
  | 'INFO_DOWNLOAD'

// ================================================================
// 中文显示名（后端 @Description EXTENSION 追加的 {field}Name 影子字段）
// ================================================================

/** 文档状态中文名 */
export type DocStatusName = '未发布' | '已发布' | '已下架'

/** 文档类型中文名 */
export type DocTypeName =
  | '隐私政策'
  | '隐私政策摘要'
  | '用户管理规则及公约'
  | '个人信息收集清单'
  | '第三方共享个人信息清单'
  | '用户协议'

/** 授权类型中文名 */
export type AuthTypeName =
  | '相机'
  | '相册'
  | '隐私政策'
  | '隐私政策摘要'
  | '用户管理规则与公约'
  | '用户协议'
  | '个人信息下载'

// ================================================================
// 出参 VO（匹配后端 Jackson 序列化：枚举名 + {field}Name 影子字段）
// ================================================================

/** 隐私文档列表摘要 VO（不含富文本） */
export interface PrivacyDocSummaryVO {
  id: number
  status: DocStatusCode
  statusName: DocStatusName
  docType: DocTypeCode
  docTypeName: DocTypeName
  version: string
  h5Url: string
  fileUrl?: string
  remark: string
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
}

/** 隐私文档详情 VO（含富文本正文） */
export interface PrivacyDocViewVO extends PrivacyDocSummaryVO {
  richContent: string
}

/** 用户授权记录 VO */
export interface AuthRecordVO {
  id: number
  openid: string
  phone: string
  authType: AuthTypeCode
  authTypeName: AuthTypeName
  version: string
  authTime: string
}

// ================================================================
// 入参 DTO
// ================================================================

/** 新增隐私文档入参（对应后端 PrivacyDocCreationVO，含 docType） */
export interface PrivacyDocCreationDTO {
  docType: DocTypeCode
  version: string
  h5Url: string
  remark?: string
  richContent: string
  fileUrl?: string
}

/** 编辑隐私文档入参（对应后端 PrivacyDocEditVO，不含 docType） */
export interface PrivacyDocEditDTO {
  version: string
  h5Url: string
  remark?: string
  richContent: string
  fileUrl?: string
}

// ================================================================
// 分页
// ================================================================

/** 分页结果（对应后端 MyBatis-Plus IPage） */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages?: number
}
