package cn.qdm.tob.modules.order.sales.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "购物车商品")
public class CartItemViewVO {
    @Schema(description = "购物车记录ID")
    private Long id;

    @Schema(description = "商品条码")
    private String barcode;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "数量")
    private BigDecimal quantity;

    @Schema(description = "是否勾选")
    private Integer selected;

    @Schema(description = "商品单价")
    private BigDecimal unitPrice;

    @Schema(description = "商品状态：LISTED=已上架 UNLISTED=已下架")
    private String productStatus;

    @Schema(description = "结算单位")
    private String unit;

    @Schema(description = "是否有效：商品不存在、价格为null或0时为false")
    private Boolean valid;
}
