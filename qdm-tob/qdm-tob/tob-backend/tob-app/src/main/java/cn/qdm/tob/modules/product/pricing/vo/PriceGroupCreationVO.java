package cn.qdm.tob.modules.product.pricing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增价格组入参
 */
@Data
@Schema(description = "新增价格组")
public class PriceGroupCreationVO {

    @Schema(description = "销售大区编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "销售大区不能为空")
    private String salesRegionCode;

    @Schema(description = "销售大区名称（冗余）")
    private String salesRegionName;

    @Schema(description = "价格组编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "价格组编码不能为空")
    private String priceGroupCode;

    @Schema(description = "价格组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "价格组名称不能为空")
    private String priceGroupName;

    @Schema(description = "描述说明")
    private String description;

    @Schema(description = "创建人")
    private String createdBy;
}
