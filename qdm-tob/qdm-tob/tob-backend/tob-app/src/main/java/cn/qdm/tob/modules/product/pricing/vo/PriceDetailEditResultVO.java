package cn.qdm.tob.modules.product.pricing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 价格组明细编辑结果 VO
 * <p>
 * 包含价格变动比率和是否需要审批的信息
 */
@Data
@Schema(description = "价格组明细编辑结果")
public class PriceDetailEditResultVO {

    @Schema(description = "是否需要审批")
    private boolean approvalRequired;

    @Schema(description = "价格变动比率（%）")
    private BigDecimal changeRatio;

    @Schema(description = "结果提示信息")
    private String message;
}
