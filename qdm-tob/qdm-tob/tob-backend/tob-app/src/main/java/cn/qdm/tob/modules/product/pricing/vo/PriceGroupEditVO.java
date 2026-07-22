package cn.qdm.tob.modules.product.pricing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 编辑价格组入参
 * <p>
 * 仅名称和描述可修改，大区和编码不可修改
 */
@Data
@Schema(description = "编辑价格组")
public class PriceGroupEditVO {

    @Schema(description = "价格组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "价格组ID不能为空")
    private Long id;

    @Schema(description = "价格组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "价格组名称不能为空")
    private String priceGroupName;

    @Schema(description = "描述说明")
    private String description;

    @Schema(description = "更新人")
    private String updatedBy;
}
