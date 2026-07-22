package cn.qdm.tob.modules.product.catalog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 小程序端商品列表出参
 */
@Data
@Schema(description = "小程序商品列表项")
public class ProductMallVO {

    @Schema(description = "商品条码")
    private String productBarcode;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "小程序名称")
    private String miniappName;

    @Schema(description = "商品主图")
    private String mainImage;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "销售大区编号")
    private String salesRegionCode;

    @Schema(description = "订购基数")
    private BigDecimal orderBaseQty;

    @Schema(description = "订购下限")
    private BigDecimal orderMinQty;

    @Schema(description = "订购上限")
    private BigDecimal orderMaxQty;
}
