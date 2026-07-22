import request from '@/utils/request'

// ================================================================
// 类型定义
// ================================================================

export interface ProductCatalogViewVO {
  id: number
  salesRegionCode: string
  salesRegionName: string
  productBarcode: string
  productName: string
  warehouseCode: string
  warehouseName: string
  status: string
  miniappName: string
  mainImage: string
  carouselImages: string
  productDetail: string
  orderBaseQty: number
  orderMinQty: number
  orderMaxQty: number
  dailyStock: number
  dailyAvailable: number
  dailySold: number
  createdBy: string
  updatedBy: string
  createdAt: string
  updatedAt: string
}

export interface ProductCatalogCreationVO {
  salesRegionCode: string
  salesRegionName: string
  productBarcode: string
  warehouseCode: string
  warehouseName: string
  status?: string
  miniappName: string
  mainImage: string
  carouselImages: string
  productDetail: string
  orderBaseQty: number
  orderMinQty: number
  orderMaxQty: number
  dailyStock: number
  createdBy: string
}

export interface ProductCatalogEditVO {
  warehouseCode: string
  warehouseName: string
  status: string
  miniappName: string
  mainImage: string
  carouselImages: string
  productDetail: string
  orderBaseQty: number
  orderMinQty: number
  orderMaxQty: number
  updatedBy: string
}

export interface StockAdjustVO {
  newDailyStock: number
}

export interface SkuLookupVO {
  barcode: string
  productName: string
  spec: string
  unit: string
}

export interface WarehouseSimpleVO {
  code: string
  name: string
  region: string
}

export interface ImportResultVO {
  createdCount: number
  updatedCount: number
  failedCount: number
  errors: ImportError[]
}

export interface ImportError {
  row: number
  reason: string
}

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

export interface ProductCatalogQuery {
  pageNum?: number
  pageSize?: number
  salesRegionCode?: string
  productBarcode?: string
  productName?: string
  status?: string
}

// ================================================================
// API 方法
// ================================================================

/** 分页查询 */
export function getProductCatalogPage(params: ProductCatalogQuery) {
  return request.get<unknown, ApiResponse<PageResult<ProductCatalogViewVO>>>(
    '/api/admin/product/catalog/list',
    { params },
  )
}

/** 详情 */
export function getProductCatalogDetail(id: number) {
  return request.get<unknown, ApiResponse<ProductCatalogViewVO>>(
    '/api/admin/product/catalog/detail',
    { params: { id } },
  )
}

/** 新增 */
export function createProductCatalog(data: ProductCatalogCreationVO) {
  return request.post<unknown, ApiResponse<null>>('/api/admin/product/catalog', data)
}

/** 编辑 */
export function updateProductCatalog(id: number, data: ProductCatalogEditVO) {
  return request.put<unknown, ApiResponse<null>>('/api/admin/product/catalog', data, {
    params: { id },
  })
}

/** 库存调整 */
export function adjustStock(id: number, data: StockAdjustVO) {
  return request.put<unknown, ApiResponse<null>>('/api/admin/product/catalog/adjust-stock', data, {
    params: { id },
  })
}

/** 条码反查 */
export function lookupBarcode(barcode: string) {
  return request.get<unknown, ApiResponse<SkuLookupVO>>(
    '/api/admin/product/catalog/lookup-barcode',
    { params: { barcode } },
  )
}

/** 仓库列表 */
export function getWarehouses(salesRegionCode?: string) {
  return request.get<unknown, ApiResponse<WarehouseSimpleVO[]>>(
    '/api/admin/product/catalog/warehouses',
    { params: { salesRegionCode } },
  )
}

/** 导出 */
export function exportProductCatalog(params: ProductCatalogQuery) {
  return request.get('/api/admin/product/catalog/export', {
    params,
    responseType: 'blob',
    headers: { Action: 'export' },
  })
}

/** 导入 */
export function importProductCatalog(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<unknown, ApiResponse<ImportResultVO>>(
    '/api/admin/product/catalog/import',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  )
}
