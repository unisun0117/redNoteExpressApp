package cn.qdm.tob.modules.product.pricing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 价格组视图（列表/详情出参）
 */
@Data
@Schema(description = "价格组视图")
public class PriceGroupViewVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "销售大区编号")
    private String salesRegionCode;

    @Schema(description = "销售大区名称")
    private String salesRegionName;

    @Schema(description = "价格组编码")
    private String priceGroupCode;

    @Schema(description = "价格组名称")
    private String priceGroupName;

    @Schema(description = "描述说明")
    private String description;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "更新人")
    private String updatedBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
