/**
 * 商品模块
 *
 * 职责: 处理商品、定价、库存、促销等商品和营销相关业务
 *
 * 子域划分:
 * - catalog: 商品目录 (商品基本信息、SKU管理)
 * - pricing: 定价 (商品价格、价格调整历史)
 * - inventory: 库存 (虚拟库存、每日限额、库存扣减)
 * - promotion: 促销活动 (促销规则、促销执行、效果统计)
 * - coupon: 优惠券 (优惠券模板、发放、使用)
 *
 * 消息事件:
 * - ProductPriceUpdatedEvent: 商品价格更新
 * - InventoryReducedEvent: 库存扣减
 * - PromotionActivatedEvent: 促销激活
 */
package cn.qdm.tob.modules.product;
