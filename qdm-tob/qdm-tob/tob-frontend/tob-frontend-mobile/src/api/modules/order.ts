/**
 * 下单 API 模块（小程序端 /api/mall/order）
 */

import { http } from '@/api/request'
import type { ApiResponse } from '@/api/request'
import type { PayParams } from './pay'

export interface OrderPreviewItem {
  barcode: string
  goodsName: string
  unit: string
  quantity: number
  unitPrice: number
  lineTotal: number
}

export interface OrderPreviewResult {
  items: OrderPreviewItem[]
  goodsAmount: number
  promotionAmount: number
  couponAmount: number
  freightAmount: number
  paidAmount: number
}

export interface OrderSubmitParams {
  addressId: number
  payMethod: string
  arrivalDate?: string
  deliveryRemark?: string
  idempotentKey?: string
}

export interface OrderSubmitResult {
  orderNo: string
  paidAmount: number
  /** 微信支付参数（仅微信支付下单成功时返回，null/undefined 时不触发支付） */
  payParams?: PayParams
}

export function previewOrder(data: {
  addressId: number
  payMethod: string
  arrivalDate?: string
}): Promise<ApiResponse<OrderPreviewResult>> {
  return http.post(`/api/mall/order/preview?addressId=${data.addressId}`, data) as Promise<ApiResponse<OrderPreviewResult>>
}

export function submitOrder(data: OrderSubmitParams): Promise<ApiResponse<OrderSubmitResult>> {
  return http.post(`/api/mall/order/submit?addressId=${data.addressId}`, data) as Promise<ApiResponse<OrderSubmitResult>>
}
