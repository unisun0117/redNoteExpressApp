/**
 * 公司收货地址模块 API（小程序端 /api/mall/customer/address）
 *
 * 对接后端客户收货地址相关接口，统一返回 ApiResponse<T>。
 * 成功码 code === 0。
 *
 * 字段名严格对齐后端 VO：
 * - list → CustomerAddressListVO
 * - detail → CustomerAddressDetailVO
 * - submit → CustomerAddressSubmitVO
 */

import { http } from '@/api/request'
import type { ApiResponse } from '@/api/request'

// ---------------------------------------------------------------------------
// 类型定义（字段名对齐后端）
// ---------------------------------------------------------------------------

/** 审核状态枚举 */
export const AuditStatus = {
  PENDING: 'PENDING',
  PASSED: 'APPROVED',
  REJECTED: 'REJECTED',
  DISABLED: 'DISABLED',
} as const

export type AuditStatusType = (typeof AuditStatus)[keyof typeof AuditStatus]

/** 公司收货地址列表项（对齐 CustomerAddressListVO） */
export interface CompanyAddressItem {
  id: string
  companyName: string
  contactName: string
  contactPhone: string
  province: string
  city: string
  district: string
  address: string
  fullAddress: string
  auditStatus: string
  auditRejectReason?: string
  createdAt: string
  isAdmin: boolean
}

/** 地址详情（对齐 CustomerAddressDetailVO） */
export interface CompanyAddressDetail extends CompanyAddressItem {
  doorPhoto: string
  licenseNo: string
  licensePhoto: string
  receiveTimeStart: string
  receiveTimeEnd: string
  receiveRequirement?: string
  storagePhotos: string[]
}

/** 提交/编辑地址入参（对齐 CustomerAddressSubmitVO） */
export interface CompanyAddressForm {
  companyName: string
  doorPhoto: string
  licenseNo: string
  licensePhoto: string
  contactName: string
  contactPhone: string
  province: string
  city: string
  district: string
  address: string
  longitude?: string
  latitude?: string
  receiveTimeStart: string
  receiveTimeEnd: string
  receiveRequirement?: string
  storagePhotos: string[]
  referralCode?: string
}

/** 推荐码验证结果 */
export interface ReferralCodeVerifyResult {
  valid: boolean
  salespersonName?: string
  message?: string
}

/** 绑定成员 */
export interface AddressMember {
  id: string
  userId: string
  userName: string
  userMobile: string
  createdAt: string
  isAdmin: boolean
}

/** 邀请码生成结果 */
export interface InviteCodeResult {
  inviteCode: string
  expireTime: string
}

// ---------------------------------------------------------------------------
// 分页响应（对齐 MyBatis Plus Page 序列化）
// ---------------------------------------------------------------------------

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

type PageResp<T> = ApiResponse<PageResult<T>>
type ListResp<T> = ApiResponse<T[]>
type SingleResp<T> = ApiResponse<T>
type NullResp = ApiResponse<null>

// ---------------------------------------------------------------------------
// API 函数
// ---------------------------------------------------------------------------

export function getAddressList(params: {
  pageNum: number
  pageSize: number
}): Promise<PageResp<CompanyAddressItem>> {
  return http.get('/api/mall/customer/address/list', { params }) as Promise<PageResp<CompanyAddressItem>>
}

export function getAddressDetail(id: string): Promise<SingleResp<CompanyAddressDetail>> {
  return http.get('/api/mall/customer/address/detail', { params: { id } }) as Promise<SingleResp<CompanyAddressDetail>>
}

export function createAddress(data: CompanyAddressForm): Promise<SingleResp<{ id: string }>> {
  return http.post('/api/mall/customer/address/create', data) as Promise<SingleResp<{ id: string }>>
}

export function updateAddress(
  id: string,
  data: Partial<CompanyAddressForm>
): Promise<SingleResp<{ id: string }>> {
  return http.post('/api/mall/customer/address/update', { ...data, id }) as Promise<SingleResp<{ id: string }>>
}

export function verifyReferralCode(referralCode: string): Promise<SingleResp<ReferralCodeVerifyResult>> {
  return http.get('/api/mall/customer/address/verify-referral', {
    params: { referralCode },
  }) as Promise<SingleResp<ReferralCodeVerifyResult>>
}

export function getMemberList(addressId: string): Promise<ListResp<AddressMember>> {
  return http.get('/api/mall/customer/address/members', {
    params: { addressId },
  }) as Promise<ListResp<AddressMember>>
}

export function generateInviteCode(addressId: string): Promise<SingleResp<InviteCodeResult>> {
  return http.post('/api/mall/customer/address/generate-invite', { addressId }) as Promise<SingleResp<InviteCodeResult>>
}

export function joinAddress(inviteCode: string): Promise<SingleResp<{ addressId: string }>> {
  return http.post('/api/mall/customer/address/join', { inviteCode }) as Promise<SingleResp<{ addressId: string }>>
}

export function unbindMember(addressId: string, userId: string): Promise<NullResp> {
  return http.post('/api/mall/customer/address/unbind', { addressId, userId }) as Promise<NullResp>
}

export interface VerifyInviteCodeResult {
  valid: boolean
  expired?: boolean
  address?: {
    companyName: string
    fullAddress: string
    receiverName: string
    receiverPhone: string
  }
}

export function verifyInviteCode(inviteCode: string): Promise<SingleResp<VerifyInviteCodeResult>> {
  return http.get('/api/mall/customer/address/verify-invite', {
    params: { inviteCode },
  }) as Promise<SingleResp<VerifyInviteCodeResult>>
}

export default {
  getAddressList,
  getAddressDetail,
  createAddress,
  updateAddress,
  verifyReferralCode,
  getMemberList,
  generateInviteCode,
  joinAddress,
  unbindMember,
  verifyInviteCode,
}
