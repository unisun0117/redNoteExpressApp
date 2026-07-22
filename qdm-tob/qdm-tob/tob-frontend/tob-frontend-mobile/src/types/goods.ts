/** 商品卡片统一数据接口（对应后端 /api/mall/product/list 出参） */
export interface GoodsItem {
  /** 商品条码 */
  productBarcode: string
  /** 商品名称 */
  productName?: string
  /** 小程序展示名称 */
  miniappName?: string
  /** 销售大区编号 */
  salesRegionCode?: string
  /** 售价 */
  price: number
  /** 商品主图 */
  mainImage?: string | null
  /** 订购基数 */
  orderBaseQty?: number
  /** 订购下限 */
  orderMinQty?: number
  /** 订购上限 */
  orderMaxQty?: number

  // ===== 购物车字段（可选，仅购物车数据携带） =====

  /** 购物车条码（同 productBarcode） */
  barcode?: string
  /** 购物车商品名称 */
  goodsName?: string
  /** 购物车商品图片 */
  productImage?: string | null
  /** 购物车商品状态 */
  productStatus?: string
  /** 购物车数量 */
  quantity?: number
  /** 购物车选中（1=选中 0=未选） */
  selected?: number
  /** 购物车单位 */
  unit?: string | null
  /** 购物车单价 */
  unitPrice?: number
  /** 购物车条目是否有效 */
  valid?: boolean

  // ===== 以下为前端展示辅助字段（可选） =====

  /** 图片水印文字 */
  watermark?: string
  /** 属性行，如 "月售1万+ | 紧实 | 扁球形" */
  attrs?: string
  /** 品质标签，如 "★ 超低客诉·口碑好货" */
  qualityTag?: string
  /** 促销红字，如 "秒杀" / "自营 特价限20袋" */
  promoText?: string
  /** 最小起售重量，如 "5斤" */
  minWeight?: string
  /** 价格单位，如 "斤" / "根" / "盒" */
  priceUnit?: string
  /** 总价展示，如 "共¥15.6" */
  totalPrice?: string
  /** 店铺名称 */
  store?: string
  /** 是否有多规格可选 */
  hasSpec?: boolean
}
