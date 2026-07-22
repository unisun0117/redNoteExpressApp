/**
 * 商品价格管理模块 API
 *
 * 对应后端 PriceGroupController + PriceDetailController
 * 路径前缀：/api/admin/product/price-group, /api/admin/product/price-detail
 */

import request from '@/utils/request'

// ================================================================
// 通用类型
// ================================================================

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface ApiResponse<T> {
  code: number
  data: T
  msg: string
}

// ================================================================
// 价格组类型
// ================================================================

export interface PriceGroupViewVO {
  id: number
  salesRegionCode: string
  salesRegionName: string
  priceGroupCode: string
  priceGroupName: string
  description: string
  createdBy: string
  updatedBy: string
  createdAt: string
  updatedAt: string
}

export interface PriceGroupCreationVO {
  salesRegionCode: string
  salesRegionName: string
  priceGroupCode: string
  priceGroupName: string
  description: string
  createdBy: string
}

export interface PriceGroupEditVO {
  id: number
  priceGroupName: string
  description: string
  updatedBy: string
}

// ================================================================
// 价格组明细类型
// ================================================================

export interface PriceDetailViewVO {
  id: number
  salesRegionCode: string
  priceGroupCode: string
  priceGroupName: string
  productBarcode: string
  productName: string
  price: number
  changeReason: string
  approvalStatus: string
  approvalStatusDescription: string
  pendingPrice: number | null
  createdBy: string
  updatedBy: string
  createdAt: string
  updatedAt: string
}

export interface PriceDetailCreationVO {
  salesRegionCode: string
  priceGroupCode: string
  priceGroupName: string
  productBarcode: string
  price: number
  createdBy: string
}

export interface PriceDetailEditVO {
  id: number
  price: number
  changeReason: string
  updatedBy: string
}

export interface PriceDetailEditResultVO {
  approvalRequired: boolean
  changeRatio: number
  message: string
}

export interface PriceDetailImportResultVO {
  createdCount: number
  updatedCount: number
  failedCount: number
  errors: Array<{ row: number; reason: string }>
}

export interface PriceDetailExportVO {
  salesRegionCode: string
  priceGroupCode: string
  priceGroupName: string
  productBarcode: string
  productName: string
  price: number
  approvalStatus: string
  updatedBy: string
  updatedAt: string
}

// ================================================================
// 价格组 API
// ================================================================

/** 分页查询价格组 */
export function getPriceGroupList(params: {
  pageNum: number
  pageSize: number
  salesRegionCode?: string
  priceGroupName?: string
}) {
  return request.get<unknown, ApiResponse<PageResult<PriceGroupViewVO>>>(
    '/api/admin/product/price-group/list',
    { params },
  )
}

/** 按大区查询价格组下拉选项 */
export function getPriceGroupOptions(salesRegionCode?: string) {
  return request.get<unknown, ApiResponse<PriceGroupViewVO[]>>(
    '/api/admin/product/price-group/options',
    { params: { salesRegionCode } },
  )
}

/** 新增价格组 */
export function createPriceGroup(data: PriceGroupCreationVO) {
  return request.post<unknown, ApiResponse<null>>('/api/admin/product/price-group', data)
}

/** 编辑价格组 */
export function updatePriceGroup(data: PriceGroupEditVO) {
  return request.put<unknown, ApiResponse<null>>('/api/admin/product/price-group', data)
}

// ================================================================
// 价格组明细 API
// ================================================================

/** 分页查询价格组明细 */
export function getPriceDetailList(params: {
  pageNum: number
  pageSize: number
  salesRegionCode?: string
  priceGroupName?: string
  keyword?: string
}) {
  return request.get<unknown, ApiResponse<PageResult<PriceDetailViewVO>>>(
    '/api/admin/product/price-detail/list',
    { params },
  )
}

/** 查询价格组明细详情 */
export function getPriceDetailDetail(id: number) {
  return request.get<unknown, ApiResponse<PriceDetailViewVO>>(
    '/api/admin/product/price-detail/detail',
    { params: { id } },
  )
}

/** 新增价格组明细 */
export function createPriceDetail(data: PriceDetailCreationVO) {
  return request.post<unknown, ApiResponse<null>>('/api/admin/product/price-detail', data)
}

/** 编辑价格组明细（含审批判断） */
export function updatePriceDetail(data: PriceDetailEditVO) {
  return request.put<unknown, ApiResponse<PriceDetailEditResultVO>>(
    '/api/admin/product/price-detail',
    data,
  )
}

/** 条码反查 */
export function lookupPriceBarcode(barcode: string) {
  return request.get<unknown, ApiResponse<PriceDetailViewVO>>(
    '/api/admin/product/price-detail/lookup-barcode',
    { params: { barcode } },
  )
}

/** 导出 */
export function exportPriceDetail(params: {
  salesRegionCode?: string
  priceGroupName?: string
  keyword?: string
}) {
  return request.get('/api/admin/product/price-detail/export', {
    params,
    responseType: 'blob',
    headers: { Action: 'export' },
  })
}

/** 导入 */
export function importPriceDetail(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<unknown, ApiResponse<PriceDetailImportResultVO>>(
    '/api/admin/product/price-detail/import',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  )
}
