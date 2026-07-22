package cn.qdm.tob.modules.product.pricing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增价格组明细入参
 */
@Data
@Schema(description = "新增价格组明细")
public class PriceDetailCreationVO {

    @Schema(description = "销售大区编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "销售大区不能为空")
    private String salesRegionCode;

    @Schema(description = "价格组编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "价格组不能为空")
    private String priceGroupCode;

    @Schema(description = "商品条码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品条码不能为空")
    private String productBarcode;

    @Schema(description = "商品名称（条码反查，不可手动输入）", hidden = true)
    private String productName;

    @Schema(description = "售价（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于0")
    @DecimalMax(value = "99999999.99", message = "售价超出上限")
    private BigDecimal price;

    @Schema(description = "创建人")
    private String createdBy;
}
