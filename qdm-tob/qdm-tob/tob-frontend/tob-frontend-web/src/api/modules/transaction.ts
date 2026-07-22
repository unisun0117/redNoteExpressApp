/**
 * 资金流水模块 API
 *
 * 对应后端 AccountTransactionController
 * 路径前缀：/api/admin/order/transaction
 */

import request from '@/utils/request'

// ===== 类型定义 =====

export interface AccountTransactionViewVO {
  id: number
  transactionNo: string
  createdAt: string
  transactionType: string
  paymentMethod: string
  customerCode: string | null
  customerName: string | null
  settlementAccountCode: string | null
  settlementAccountName: string | null
  operatorName: string | null
  incomeExpenseType: string
  amount: number
  balanceAfter: number | null
  businessNo: string | null
  orderNo: string | null
  thirdPartyFlowNo: string | null
  status: string
  remark: string | null
}

export interface TransactionSummaryVO {
  incomeAmount: number
  incomeCount: number
  expenseAmount: number
  expenseCount: number
}

export interface TransactionQuery {
  pageNum: number
  pageSize: number
  startTime?: string
  endTime?: string
  incomeExpenseType?: string
  customerKeyword?: string
  settlementKeyword?: string
  paymentMethod?: string
  transactionType?: string
  thirdPartyFlowNo?: string
  businessNo?: string
  flowNo?: string
  orderNo?: string
  operatorKeyword?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  pages: number
}

interface ApiResponse<T> {
  code: number
  data: T
  msg: string
}

/** 分页查询 */
export function getTransactionPage(params: TransactionQuery): Promise<ApiResponse<PageResult<AccountTransactionViewVO>>> {
  return request.get('/api/admin/order/transaction/list', { params })
}

/** 汇总统计 */
export function getTransactionSummary(params: Omit<TransactionQuery, 'pageNum' | 'pageSize'>): Promise<ApiResponse<TransactionSummaryVO>> {
  return request.get('/api/admin/order/transaction/summary', { params })
}

const TYPE_MAP: Record<string, string> = {
  RECHARGE: '充值', TRANSFER: '转账', REFUND: '退款',
  WITHDRAW: '提现', ORDER: '下单', RECEIPT: '收款',
}
const METHOD_MAP: Record<string, string> = {
  WECHAT: '微信', PREPAID: '预付款', CREDIT: '账期', BANK_CARD: '银行卡',
}
const STATUS_MAP: Record<string, string> = {
  PROCESSING: '处理中', SUCCESS: '成功', FAILED: '失败',
}

export function typeLabel(v: string) { return TYPE_MAP[v] || v }
export function methodLabel(v: string) { return METHOD_MAP[v] || v }
export function statusLabel(v: string) { return STATUS_MAP[v] || v }
