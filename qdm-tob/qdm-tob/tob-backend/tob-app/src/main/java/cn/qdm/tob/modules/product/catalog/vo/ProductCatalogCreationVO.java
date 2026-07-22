package cn.qdm.tob.modules.product.catalog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增商品资料入参 VO
 */
@Data
@Schema(description = "新增商品资料")
public class ProductCatalogCreationVO {

    @Schema(description = "销售大区编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "销售大区不能为空")
    private String salesRegionCode;

    @Schema(description = "销售大区名称（冗余）")
    private String salesRegionName;

    @Schema(description = "商品条码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品条码不能为空")
    private String productBarcode;

    @Schema(description = "商品名称（条码反查，不可手动输入）", hidden = true)
    private String productName;

    @Schema(description = "仓库编码（SAP）")
    private String warehouseCode;

    @Schema(description = "仓库名称（冗余）")
    private String warehouseName;

    @Schema(description = "状态", defaultValue = "LISTED")
    private String status;

    @Schema(description = "小程序名称（商品别名）")
    private String miniappName;

    @Schema(description = "商品主图 URL")
    private String mainImage;

    @Schema(description = "轮播图 URL 列表（JSON 数组）")
    private String carouselImages;

    @Schema(description = "商品详情（富文本）")
    private String productDetail;

    @Schema(description = "订购基数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订购基数不能为空")
    @DecimalMin(value = "0", message = "订购基数不能小于0")
    @DecimalMax(value = "99999999.99", message = "订购基数超出上限")
    private BigDecimal orderBaseQty;

    @Schema(description = "订购下限", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订购下限不能为空")
    @DecimalMin(value = "0", message = "订购下限不能小于0")
    @DecimalMax(value = "99999999.99", message = "订购下限超出上限")
    private BigDecimal orderMinQty;

    @Schema(description = "订购上限", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订购上限不能为空")
    @DecimalMin(value = "0", message = "订购上限不能小于0")
    @DecimalMax(value = "99999999.99", message = "订购上限超出上限")
    private BigDecimal orderMaxQty;

    @Schema(description = "每日可用库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "每日可用库存不能为空")
    @DecimalMin(value = "0", message = "每日可用库存不能小于0")
    @DecimalMax(value = "99999999.99", message = "每日可用库存超出上限")
    private BigDecimal dailyStock;

    @Schema(description = "创建人")
    private String createdBy;
}
