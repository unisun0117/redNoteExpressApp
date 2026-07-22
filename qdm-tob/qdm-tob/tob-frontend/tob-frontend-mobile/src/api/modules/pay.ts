/**
 * 微信支付模块 API（小程序端 /api/mall/pay）
 *
 * 对接后端 WxPayController，统一返回 ApiResponse<T>。
 */

import { http } from '@/api/request'
import type { ApiResponse } from '@/api/request'

// ---------------------------------------------------------------------------
// 类型定义
// ---------------------------------------------------------------------------

/** 下单请求参数 */
export interface CreateOrderParams {
  /** 商品描述 */
  description: string
  /** 商户订单号（6-32位，数字/字母/_-|*） */
  outTradeNo: string
  /** 订单金额（单位：元） */
  amountYuan: number
  /** 附加数据（可选），支付成功后回调原样返回 */
  attach?: string
}

/** 调起支付所需参数 */
export interface PayParams {
  appId: string
  timeStamp: string
  nonceStr: string
  packageStr: string
  signType: string
  paySign: string
}

type PayResp = ApiResponse<PayParams>

// ---------------------------------------------------------------------------
// API 方法
// ---------------------------------------------------------------------------

/** 创建支付订单：POST /api/mall/pay/orders */
export function createOrder(data: CreateOrderParams): Promise<PayResp> {
  return http.post('/api/mall/pay/orders', data) as Promise<PayResp>
}
