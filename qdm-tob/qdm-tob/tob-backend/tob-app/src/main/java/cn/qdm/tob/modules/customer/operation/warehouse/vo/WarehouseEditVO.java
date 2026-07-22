package cn.qdm.tob.modules.customer.operation.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "编辑仓库")
public class WarehouseEditVO {
    @NotBlank @Schema(description = "仓库名称") private String name;
    @NotBlank @Schema(description = "销售大区编码") private String region;
    @NotBlank @Schema(description = "仓库性质") private String type;
    @Schema(description = "省名称") private String provinceName;
    @Schema(description = "市名称") private String cityName;
    @Schema(description = "区名称") private String districtName;
    @Schema(description = "省") private String province;
    @Schema(description = "市") private String city;
    @Schema(description = "区") private String district;
    @Schema(description = "详细地址") private String address;
    @Schema(description = "经度") private BigDecimal lng;
    @Schema(description = "纬度") private BigDecimal lat;
    @Schema(description = "修改人") private String updatedBy;
}
