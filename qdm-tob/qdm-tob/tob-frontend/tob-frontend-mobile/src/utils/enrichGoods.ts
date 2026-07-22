/**
 * 商品展示字段通用填充器
 *
 * 根据后端已有字段派生前端展示用的辅助字段，纯 UI 层逻辑，可随时整体删除。
 *
 * 使用方式：
 *   import { enrichGoodsList } from '@/utils/enrichGoods'
 *   const enriched = enrichGoodsList(apiResponse)
 */

import type { GoodsItem } from '@/types/goods'

// ---- 品质标签候选池 ----
const QUALITY_TAGS = [
  '★ 超低客诉·口碑好货',
  '★ 复购率高·老客最爱',
  '★ 新鲜直达·产地直发',
  '★ 精选优品·严格品控',
  '★ 热卖爆款·销量领先',
]

// ---- 促销文案候选池（大部分为空，降低密度） ----
const PROMO_TEXTS = ['', '', '', '限时特惠', '新品尝鲜', '今日特价']

// ---- attrs 属性描述候选池 ----
const ATTRS_FALLBACK = [
  '月售1万+ | 紧实 | 扁球形',
  '月售8000+ | 饱满 | 色泽鲜亮',
  '月售5000+ | 新鲜 | 产地直供',
  '人气爆款 | 品质稳定 | 复购率高',
  '严选好货 | 个头均匀 | 口感佳',
]

/** 从 GoodsItem 上安全读取非标准字段 */
function get(item: GoodsItem, key: string): string | undefined {
  return (item as unknown as Record<string, unknown>)[key] as string | undefined
}

/** 为单条商品填充展示辅助字段 */
export function enrichGoodsItem(item: GoodsItem): GoodsItem {
  const unit = item.unit || get(item, 'order_unit') || ''

  // 价格单位：优先购物车 unit
  if (!item.priceUnit && unit) {
    item.priceUnit = unit
  }

  // 最小起订展示
  if (!item.minWeight && item.orderMinQty && unit) {
    item.minWeight = `${item.orderMinQty}${unit}`
  }

  // 属性行：优先 spec，否则用 stable fallback
  if (!item.attrs) {
    const spec = get(item, 'spec')
    if (spec) {
      item.attrs = spec
    } else {
      item.attrs = ATTRS_FALLBACK[hashCode(item.productBarcode + '_attrs') % ATTRS_FALLBACK.length]
    }
  }

  // 品质标签（基于条码 hashCode 稳定分配）
  if (!item.qualityTag) {
    item.qualityTag = QUALITY_TAGS[hashCode(item.productBarcode) % QUALITY_TAGS.length]
  }

  // 促销文案（随机，大部分为空）
  if (!item.promoText) {
    item.promoText = PROMO_TEXTS[hashCode(item.productBarcode + '_promo') % PROMO_TEXTS.length] || undefined
  }

  // 总价展示
  if (!item.totalPrice && item.price != null && item.orderBaseQty) {
    const total = (Number(item.price) * Number(item.orderBaseQty)).toFixed(1)
    item.totalPrice = `共¥${total}`
  }

  // 商品图片兜底（购物车数据用 productImage）
  if (!item.mainImage) {
    const img = get(item, 'productImage')
    if (img) item.mainImage = img
  }

  return item
}

/** 批量填充商品列表的展示辅助字段 */
export function enrichGoodsList(list: GoodsItem[]): GoodsItem[] {
  return list.map(enrichGoodsItem)
}

/** 简易字符串 hashCode */
function hashCode(s: string): number {
  let hash = 0
  for (let i = 0; i < s.length; i++) {
    hash = ((hash << 5) - hash + s.charCodeAt(i)) | 0
  }
  return Math.abs(hash)
}
