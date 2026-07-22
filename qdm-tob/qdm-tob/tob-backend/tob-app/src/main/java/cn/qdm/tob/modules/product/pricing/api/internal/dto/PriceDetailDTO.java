package cn.qdm.tob.modules.product.pricing.api.internal.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 价格明细 DTO，供订单模块跨模块查询
 */
@Data
public class PriceDetailDTO {

    /** 商品条码 */
    private String productBarcode;

    /** 价格组编码 */
    private String priceGroupCode;

    /** 售价（元） */
    private BigDecimal price;
}
