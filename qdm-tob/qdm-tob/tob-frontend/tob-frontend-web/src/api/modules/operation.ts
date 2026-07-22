/**
 * 运营管理模块 API
 *
 * 对应后端 SalesRegionController
 * 路径前缀：/api/admin/operation/sales-region
 */

import request from '@/utils/request'

// ===== 类型定义 =====

/** 销售大区查询参数 */
export interface SalesRegionQuery {
  pageNum: number
  pageSize: number
  region?: string
  serviceEnabled?: boolean
}

/** 新增大区参数 */
export interface SalesRegionCreationVO {
  code: string
  name: string
  serviceEnabled?: boolean
  multiDay?: boolean
  minDays?: number
  bizHours?: string
  orderType?: string
  orderAmount?: string
  arrivalDays?: number
  priceApproval?: boolean
  approvalThreshold?: number
  approvers?: string
  stdFreight?: number
  stdFreeAmount?: number
  newFreight?: number
  newFreeAmount?: number
  merchantNo: string
  merchantName?: string
  regionManagers?: string
  coveredCities?: string
  createdBy?: string
}

/** 编辑大区参数 */
export interface SalesRegionEditVO {
  name: string
  serviceEnabled?: boolean
  multiDay?: boolean
  minDays?: number
  bizHours?: string
  orderType?: string
  orderAmount?: string
  arrivalDays?: number
  priceApproval?: boolean
  approvalThreshold?: number
  approvers?: string
  stdFreight?: number
  stdFreeAmount?: number
  newFreight?: number
  newFreeAmount?: number
  merchantNo?: string
  merchantName?: string
  regionManagers?: string
  coveredCities?: string
  updatedBy?: string
}

/** 销售大区详情 */
export interface SalesRegionViewVO {
  id: number
  code: string
  name: string
  serviceEnabled: boolean
  multiDay: boolean
  minDays: number | null
  bizHours: string
  orderType: string
  orderAmount: string
  arrivalDays: number
  priceApproval: boolean
  approvalThreshold: number | null
  approvers: string | null
  warehouseCount: number
  stdFreight: number
  stdFreeAmount: number
  newFreight: number
  newFreeAmount: number
  merchantNo: string
  merchantName: string
  regionManagers: string | null
  coveredCities: string | null
  createdBy: string
  updatedBy: string
  updatedAt: string
  createdAt: string
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

/** 分页查询 */
export function getSalesRegionList(
  params: SalesRegionQuery,
): Promise<ApiResponse<PageResult<SalesRegionViewVO>>> {
  return request.get('/api/admin/operation/sales-region/list', { params })
}

/** 查询详情 */
export function getSalesRegionDetail(code: string): Promise<ApiResponse<SalesRegionViewVO>> {
  return request.get('/api/admin/operation/sales-region/detail', { params: { code } })
}

/** 新增 */
export function createSalesRegion(data: SalesRegionCreationVO): Promise<ApiResponse<null>> {
  return request.post('/api/admin/operation/sales-region', data)
}

/** 编辑 */
export function updateSalesRegion(
  code: string,
  data: SalesRegionEditVO,
): Promise<ApiResponse<null>> {
  return request.put('/api/admin/operation/sales-region', data, { params: { code } })
}

/** 删除 */
export function deleteSalesRegion(code: string): Promise<ApiResponse<null>> {
  return request.delete('/api/admin/operation/sales-region', { params: { code } })
}

/** 查询全部大区（下拉选项用，不分页） */
export function getAllSalesRegions(): Promise<ApiResponse<PageResult<SalesRegionViewVO>>> {
  return request.get('/api/admin/operation/sales-region/list', {
    params: { pageNum: 1, pageSize: 9999 },
  })
}
