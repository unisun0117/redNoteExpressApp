/**
 * 商品 API 模块（小程序端 /api/mall/products）
 */

import { http } from '@/api/request'
import type { ApiResponse } from '@/api/request'

export interface ProductVO {
  productBarcode: string
  productName: string
  miniappName: string
  mainImage: string
  price: number
  salesRegionCode: string
  orderBaseQty: number
  orderMinQty: number
  orderMaxQty: number
}

export interface ProductPageResult {
  records: ProductVO[]
  total: number
  size: number
  current: number
  pages: number
}

export function getProductList(addressId: number, page = 1, size = 20): Promise<ApiResponse<ProductPageResult>> {
  return http.get('/api/mall/products', { params: { addressId, page, size } }) as Promise<ApiResponse<ProductPageResult>>
}

export interface CategoryVO {
  id: string
  name: string
  alias: string
}

export function getCategories(): Promise<ApiResponse<CategoryVO[]>> {
  return http.get('/api/mall/categories') as Promise<ApiResponse<CategoryVO[]>>
}
