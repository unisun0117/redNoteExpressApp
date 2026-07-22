package cn.qdm.tob.modules.order.sales.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "订单预览商品明细")
public class OrderPreviewItemVO {
    @Schema(description = "商品条码")
    private String barcode;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "结算单位")
    private String unit;

    @Schema(description = "数量")
    private BigDecimal quantity;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "行金额")
    private BigDecimal lineTotal;
}
