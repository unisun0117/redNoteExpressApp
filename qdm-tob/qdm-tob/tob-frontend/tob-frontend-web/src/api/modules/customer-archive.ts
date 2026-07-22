/**
 * 客户档案管理 API
 */

import request from '@/utils/request'

// ===== 类型定义 =====

/**
 * 分页查询参数
 */
export interface CustomerArchiveQuery {
  pageNum?: number
  pageSize?: number
  companyName?: string
  salesRegionName?: string
  province?: string
  city?: string
  district?: string
  salesmanName?: string
  auditStatus?: AuditStatus
  auditorName?: string
}

/**
 * 审核状态枚举
 */
export type AuditStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

/**
 * 结算类型枚举
 */
export type SettleType = 'CASH' | 'PERIOD'

/**
 * 客户档案列表项
 */
export interface CustomerArchiveSummaryVO {
  id: number
  sapCustomerCode?: string
  companyName: string
  contactName: string
  contactPhone: string
  province: string
  city: string
  district: string
  address: string
  salesRegionName?: string
  salesmanName?: string
  auditStatus: AuditStatus
  priceGroup?: string
  settleCompany?: string
  businessType?: string
  settleType?: SettleType
  auditorName?: string
  lastOrderTime?: string
  createdAt: string
  boundUserCount: number
}

/**
 * 客户档案详情
 */
export interface CustomerArchiveViewVO {
  id: number
  sapCustomerCode?: string
  companyName: string
  licenseNo?: string
  doorPhoto?: string
  licensePhoto?: string
  storagePhotos?: string
  contactName: string
  contactPhone: string
  province: string
  city: string
  district: string
  address: string
  longitude?: number
  latitude?: number
  receiveTimeStart?: string
  receiveTimeEnd?: string
  receiveRequirement?: string
  salesRegionId?: number
  salesRegionName?: string
  salesmanId?: number
  salesmanName?: string
  auditStatus: AuditStatus
  priceGroup?: string
  settleCompany?: string
  businessType?: string
  settleType?: SettleType
  internalRemark?: string
  auditorId?: number
  auditorName?: string
  auditorType?: string
  auditRejectReason?: string
  auditTime?: string
  submitUserId?: number
  submitUserName?: string
  lastOrderTime?: string
  createdAt: string
  updatedAt: string
  auditLogs?: AuditLogVO[]
  boundUsers?: BoundUserVO[]
}

/**
 * 审核历史记录
 */
export interface AuditLogVO {
  id: number
  createdAt: string
  action: string
  operatorName?: string
  remark?: string
}

/**
 * 已绑定用户（列表/详情通用）
 */
export interface BoundUserVO {
  id?: number
  userId: number
  userName?: string
  userMobile?: string
  memberRole?: string
  bindingStatus?: string
  createdAt?: string
}

/**
 * 已绑定用户别名，方便简化命名
 */
export type BoundUser = BoundUserVO

/**
 * 新增客户档案参数
 */
export interface CustomerArchiveCreateVO {
  companyName: string
  licenseNo: string
  doorPhoto?: string
  licensePhoto?: string
  contactName: string
  contactPhone: string
  province: string
  city: string
  district: string
  address: string
  longitude?: string
  latitude?: string
  receiveTimeStart?: string
  receiveTimeEnd?: string
  receiveRequirement?: string
  salesRegionName?: string
}

/**
 * 编辑业务属性参数
 */
export interface CustomerArchiveEditVO {
  contactName?: string
  contactPhone?: string
  priceGroup?: string
  settleCompany?: string
  businessType?: string
  settleType?: SettleType
  internalRemark?: string
}

/**
 * 分配审核人参数
 */
export interface AssignAuditorVO {
  auditorId: number
  auditorName?: string
  auditorType?: string
}

/**
 * 分页响应
 */
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  pages: number
}

/**
 * 统一响应包装
 */
interface ApiResponse<T> {
  code: number
  data: T
  msg: string
}

// ===== API 方法 =====

/**
 * 分页查询客户档案列表
 */
export function getCustomerArchivePage(
  params: CustomerArchiveQuery,
): Promise<ApiResponse<PageResult<CustomerArchiveSummaryVO>>> {
  return request.get('/api/admin/customer-archive/page', { params })
}

/**
 * 查询客户档案详情
 */
export function getCustomerArchiveDetail(
  id: number,
): Promise<ApiResponse<CustomerArchiveViewVO>> {
  return request.get('/api/admin/customer-archive/detail', { params: { id } })
}

/**
 * 后台新增客户档案
 */
export function createCustomerArchive(
  data: CustomerArchiveCreateVO,
): Promise<ApiResponse<null>> {
  return request.post('/api/admin/customer-archive/create', data)
}

/**
 * 编辑业务属性
 */
export function editCustomerArchive(
  id: number,
  data: CustomerArchiveEditVO,
): Promise<ApiResponse<null>> {
  return request.post('/api/admin/customer-archive/edit', data, { params: { id } })
}

/**
 * 手动分配审核人
 */
export function assignAuditor(
  id: number,
  data: AssignAuditorVO,
): Promise<ApiResponse<null>> {
  return request.post('/api/admin/customer-archive/assign-auditor', data, { params: { id } })
}

/**
 * 查询已绑定用户列表
 */
export function getBoundUsers(
  id: number,
): Promise<ApiResponse<BoundUserVO[]>> {
  return request.get('/api/admin/customer-archive/bound-users', { params: { id } })
}

/**
 * 搜索小程序用户（用于绑定）
 */
export function searchUsers(
  keyword?: string,
): Promise<ApiResponse<BoundUserVO[]>> {
  return request.get('/api/admin/customer-archive/search-users', { params: { keyword } })
}

/**
 * 绑定小程序用户
 */
export function bindUser(
  archiveId: number,
  userId: number,
  userName?: string,
  userMobile?: string,
): Promise<ApiResponse<null>> {
  return request.post('/api/admin/customer-archive/bind-user', null, {
    params: { id: archiveId, userId, userName, userMobile }
  })
}

/**
 * 业务员推荐码列表项（来自 /api/admin/salesman/referral/list）
 */
export interface SalesmanReferralVO {
  id: number
  userId: number
  name: string
  phone: string
  referralCode?: string
  codeStatus?: string
  customerCount?: number
}

/**
 * 获取全部业务员推荐码列表（下拉选项用）
 */
export function getSalesmanReferralList(): Promise<ApiResponse<PageResult<SalesmanReferralVO>>> {
  return request.get('/api/admin/salesman/referral/list', {
    params: { pageNum: 1, pageSize: 9999 },
  })
}

/**
 * 解绑小程序用户
 */
export function unbindUser(
  archiveId: number,
  userId: number,
): Promise<ApiResponse<null>> {
  return request.post('/api/admin/customer-archive/unbind-user', null, {
    params: { id: archiveId, userId }
  })
}
