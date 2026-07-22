/**
 * 购物车 API 模块（小程序端 /api/mall/cart）
 */

import { http } from '@/api/request'
import type { ApiResponse } from '@/api/request'

export interface CartItemVO {
  id: number
  barcode: string
  goodsName: string
  productImage: string
  quantity: number
  selected: number
  unitPrice: number
  productStatus: string
  unit: string
  valid: boolean
}

type NullResp = ApiResponse<null>
type CartListResp = ApiResponse<CartItemVO[]>

export function addToCart(barcode: string, quantity: number): Promise<NullResp> {
  return http.post('/api/mall/cart/add', { barcode, quantity }) as Promise<NullResp>
}

export function updateCart(id: number, data: { quantity?: number; selected?: number }): Promise<NullResp> {
  return http.put('/api/mall/cart/update', { id, ...data }) as Promise<NullResp>
}

export function removeCartItem(id: number): Promise<NullResp> {
  return http.delete(`/api/mall/cart/remove?id=${id}`) as Promise<NullResp>
}

export function getCartList(addressId: number): Promise<CartListResp> {
  return http.get('/api/mall/cart/list', { params: { addressId } }) as Promise<CartListResp>
}

export function removeCartBatch(ids: number[]): Promise<NullResp> {
  const params = ids.map(id => `ids=${id}`).join('&')
  return http.delete(`/api/mall/cart/remove/batch?${params}`) as Promise<NullResp>
}
