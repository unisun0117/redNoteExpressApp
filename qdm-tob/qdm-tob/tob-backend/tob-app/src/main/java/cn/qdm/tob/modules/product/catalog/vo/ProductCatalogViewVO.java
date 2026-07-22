package cn.qdm.tob.modules.product.catalog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品资料列表/详情出参 VO
 */
@Data
@Schema(description = "商品资料")
public class ProductCatalogViewVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "销售大区编号")
    private String salesRegionCode;

    @Schema(description = "销售大区名称")
    private String salesRegionName;

    @Schema(description = "商品条码")
    private String productBarcode;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "仓库编码（SAP）")
    private String warehouseCode;

    @Schema(description = "仓库名称")
    private String warehouseName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "小程序名称")
    private String miniappName;

    @Schema(description = "商品主图 URL")
    private String mainImage;

    @Schema(description = "轮播图 URL 列表")
    private String carouselImages;

    @Schema(description = "商品详情（富文本）")
    private String productDetail;

    @Schema(description = "订购基数")
    private BigDecimal orderBaseQty;

    @Schema(description = "订购下限")
    private BigDecimal orderMinQty;

    @Schema(description = "订购上限")
    private BigDecimal orderMaxQty;

    @Schema(description = "每日可用库存")
    private BigDecimal dailyStock;

    @Schema(description = "今日可用数量")
    private BigDecimal dailyAvailable;

    @Schema(description = "今日已售数量")
    private BigDecimal dailySold;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "修改人")
    private String updatedBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;
}
