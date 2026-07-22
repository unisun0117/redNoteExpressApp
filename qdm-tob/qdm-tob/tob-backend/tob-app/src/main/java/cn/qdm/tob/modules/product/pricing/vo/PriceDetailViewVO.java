package cn.qdm.tob.modules.product.pricing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格组明细视图（列表/详情出参）
 */
@Data
@Schema(description = "价格组明细视图")
public class PriceDetailViewVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "销售大区编号")
    private String salesRegionCode;

    @Schema(description = "价格组编码")
    private String priceGroupCode;

    @Schema(description = "价格组名称")
    private String priceGroupName;

    @Schema(description = "商品条码")
    private String productBarcode;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "售价（元）")
    private BigDecimal price;

    @Schema(description = "变动原因")
    private String changeReason;

    @Schema(description = "审批状态")
    private String approvalStatus;

    @Schema(description = "审批状态描述")
    private String approvalStatusDescription;

    @Schema(description = "待审批的新价格")
    private BigDecimal pendingPrice;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "更新人")
    private String updatedBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
