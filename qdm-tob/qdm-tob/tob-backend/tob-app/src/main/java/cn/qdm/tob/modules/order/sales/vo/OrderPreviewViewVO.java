package cn.qdm.tob.modules.order.sales.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "订单预览结果")
public class OrderPreviewViewVO {
    @Schema(description = "商品明细")
    private List<OrderPreviewItemVO> items;

    @Schema(description = "商品总金额")
    private BigDecimal goodsAmount;

    @Schema(description = "促销金额（本期为0）")
    private BigDecimal promotionAmount;

    @Schema(description = "优惠券金额（本期为0）")
    private BigDecimal couponAmount;

    @Schema(description = "运费")
    private BigDecimal freightAmount;

    @Schema(description = "实付金额")
    private BigDecimal paidAmount;
}
