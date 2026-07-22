/**
 * 客户账户管理模块 API
 *
 * 对应后端 CustomerAccountController
 * 路径前缀：/api/admin/order/account
 */

import request from '@/utils/request'

// ===== 类型定义 =====

/** 账户类型 */
export type AccountType = 'PREPAID' | 'CREDIT'

/** 流水类型 */
export type TransactionType = 'RECHARGE' | 'WITHDRAW'

/** 客户账户列表行 */
export interface AccountSummaryVO {
  id: number
  customerCode: string
  customerName: string
  balance: number
  accountType: AccountType
  creditDays: number | null
  nextReconciliationDate: string | null
  remark: string | null
  licenseNo: string | null
  companyName: string | null
  licensePhoto: string | null
  createdAt: string
}

/** 客户信息 */
export interface CustomerInfoVO {
  customerCode: string
  customerName: string
  licenseNo: string | null
  companyName: string | null
  licensePhoto: string | null
}

/** 新增流水入参 */
export interface TransactionCreateVO {
  customerCode: string
  accountType: AccountType
  transactionType: TransactionType
  amount: number
}

/** 分页查询参数 */
export interface AccountQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  licenseNo?: string
  accountType?: AccountType
}

/** 分页响应 */
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  pages: number
}

/** 统一响应包装 */
interface ApiResponse<T> {
  code: number
  data: T
  msg: string
}

// ===== API 方法 =====

/** 分页查询客户账户 */
export function getAccountPage(
  params: AccountQuery,
): Promise<ApiResponse<PageResult<AccountSummaryVO>>> {
  return request.get('/api/admin/order/account/list', { params })
}

/** 按客户编码查询客户档案信息 */
export function getCustomerByCode(code: string): Promise<ApiResponse<CustomerInfoVO>> {
  return request.get('/api/admin/order/account/customer', { params: { code } })
}

/** 新增充值/提现流水 */
export function createTransaction(data: TransactionCreateVO): Promise<ApiResponse<null>> {
  return request.post('/api/admin/order/account/transaction', data)
}
