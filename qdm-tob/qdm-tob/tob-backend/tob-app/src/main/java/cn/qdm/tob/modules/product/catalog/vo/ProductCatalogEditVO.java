package cn.qdm.tob.modules.product.catalog.vo;

import cn.qdm.tob.modules.product.catalog.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 编辑商品资料入参 VO
 */
@Data
@Schema(description = "编辑商品资料")
public class ProductCatalogEditVO {

    @Schema(description = "仓库编码（SAP）")
    private String warehouseCode;

    @Schema(description = "仓库名称（冗余）")
    private String warehouseName;

    @Schema(description = "状态：LISTED=已上架 UNLISTED=已下架")
    private ProductStatus status;

    @Schema(description = "小程序名称（商品别名）")
    private String miniappName;

    @Schema(description = "商品主图 URL")
    private String mainImage;

    @Schema(description = "轮播图 URL 列表（JSON 数组）")
    private String carouselImages;

    @Schema(description = "商品详情（富文本）")
    private String productDetail;

    @Schema(description = "订购基数")
    @NotNull(message = "订购基数不能为空")
    @DecimalMin(value = "0")
    @DecimalMax(value = "99999999.99")
    private BigDecimal orderBaseQty;

    @Schema(description = "订购下限")
    @NotNull(message = "订购下限不能为空")
    @DecimalMin(value = "0")
    @DecimalMax(value = "99999999.99")
    private BigDecimal orderMinQty;

    @Schema(description = "订购上限")
    @NotNull(message = "订购上限不能为空")
    @DecimalMin(value = "0")
    @DecimalMax(value = "99999999.99")
    private BigDecimal orderMaxQty;

    @Schema(description = "修改人")
    private String updatedBy;
}
