package cn.qdm.tob.modules.product;

import org.springframework.modulith.ApplicationModule;

/**
 * 商品模块
 *
 * 职责: 处理商品、定价、库存、促销等商品和营销相关业务
 *
 * 子域:
 * - catalog: 商品目录 (商品基本信息、SKU管理)
 * - pricing: 定价 (商品价格、价格调整)
 * - inventory: 库存 (虚拟库存、每日限额)
 * - promotion: 促销活动 (促销规则、执行)
 * - coupon: 优惠券 (优惠券模板、发放、使用)
 */
@ApplicationModule(displayName = "Product Module")
public class ProductModule {
}
