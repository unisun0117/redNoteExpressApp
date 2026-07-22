/**
 * 商品基础资料管理模块 API
 *
 * 对应后端 ProductController
 * 路径前缀：/api/public/admin/product
 */

import request from '@/utils/request'

// ===== 类型定义 =====

/** 商品资料列表行 */
export interface ProductViewVO {
  productId: number
  barcode: string
  name: string
  spu: string | null
  categoryId: string
  spec: string | null
  originPlace: string | null
  brand: string | null
  unit1: string
  unit2: string
  unitWeight: number | null
  qualityDays: number | null
  seasonFactor: string | null
  storageReq: string | null
  remark: string | null
  status: string
  createdBy: string | null
  updatedBy: string | null
  createdAt: string
  updatedAt: string
}

/** 分页查询参数 */
export interface ProductQuery {
  pageNum: number
  pageSize: number
  barcode?: string
  name?: string
  categoryId?: string
  status?: string
}

/** 分页响应 */
interface PageResult<T> {
  records: T[]
  total: number
  current: number
  pages: number
}

/** 统一响应包装 */
interface ApiResponse<T> {
  code: number
  data: T
  msg: string | null
}

// ===== API 方法 =====

/** 分页查询商品资料列表 */
export function getProductPage(
  params: ProductQuery,
): Promise<ApiResponse<PageResult<ProductViewVO>>> {
  return request.get('/api/public/admin/product/list', { params })
}
