/**
 * 业务员管理模块 API
 *
 * 对应后端 SalesmanController
 * 路径前缀：/api/admin/salesman
 */

import request from '@/utils/request'

// ===== 类型定义 =====

/** 业务员推荐码 */
export interface SalesmanReferralVO {
  id: string
  userId: string
  name: string
  phone: string
  referralCode: string
  codeStatus: 'VALID' | 'EMPTY' | 'INVALID'
  customerCount: number
  updatedBy: string
  updatedAt: string
}

/** 添加业务员参数 */
export interface AddSalesmanVO {
  userId: string
  createdBy?: string
}

/** 业务员绩效 */
export interface SalesmanPerformanceVO {
  id?: string
  salesmanId: string | null
  salesmanName?: string
  month: string
  orderCount: number
  customerCount: number
  salesAmount: number
  updatedBy: string
  updatedAt: string
}

/** 分页响应 */
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

// ===== API 方法 =====

/** 分页查询业务员推荐码 */
export function getSalesmanReferralList(params: {
  pageNum: number
  pageSize: number
  name?: string
  phone?: string
}): Promise<ApiResponse<PageResult<SalesmanReferralVO>>> {
  return request.get('/api/admin/salesman/referral/list', { params })
}

/** 添加业务员 */
export function addSalesman(data: AddSalesmanVO): Promise<ApiResponse<SalesmanReferralVO>> {
  return request.post('/api/admin/salesman/add', data)
}

/** 重新生成推荐码 */
export function regenerateCode(id: string, updatedBy?: string): Promise<ApiResponse<null>> {
  return request.post('/api/admin/salesman/referral/regenerate', null, {
    params: { id, updatedBy },
  })
}

/** 置空推荐码 */
export function clearCode(id: string, updatedBy?: string): Promise<ApiResponse<null>> {
  return request.post('/api/admin/salesman/referral/clear', null, {
    params: { id, updatedBy },
  })
}

/** 删除推荐码（标记失效） */
export function deleteReferralCode(id: string, updatedBy?: string): Promise<ApiResponse<null>> {
  return request.delete('/api/admin/salesman/referral', { params: { id, updatedBy } })
}

/** 获取全部业务员（下拉选项用） */
export function getAllSalesmen(): Promise<ApiResponse<SalesmanReferralVO[]>> {
  return request.get('/api/admin/salesman/list-all')
}

/** 分页查询绩效列表 */
export function getPerformanceList(params: {
  pageNum: number
  pageSize: number
  salesmanId?: string
  month?: string
}): Promise<ApiResponse<PageResult<SalesmanPerformanceVO>>> {
  return request.get('/api/admin/salesman/performance/list', { params })
}

/** 新增绩效 */
export function addPerformance(
  data: SalesmanPerformanceVO,
  updatedBy?: string,
): Promise<ApiResponse<null>> {
  return request.post('/api/admin/salesman/performance', data, {
    params: { updatedBy },
  })
}

/** 编辑绩效 */
export function editPerformance(
  data: SalesmanPerformanceVO,
  updatedBy?: string,
): Promise<ApiResponse<null>> {
  return request.put('/api/admin/salesman/performance', data, {
    params: { updatedBy },
  })
}
