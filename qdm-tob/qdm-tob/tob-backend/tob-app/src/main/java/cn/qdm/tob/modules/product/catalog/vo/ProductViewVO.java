package cn.qdm.tob.modules.product.catalog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品资料出参 VO
 */
@Data
@Schema(description = "商品资料")
public class ProductViewVO {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品编码（SAP业务系统编号）")
    private String barcode;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "SPU编码")
    private String spu;

    @Schema(description = "小类编号")
    private String categoryId;

    @Schema(description = "规格")
    private String spec;

    @Schema(description = "产地")
    private String originPlace;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "订购单位")
    private String unit1;

    @Schema(description = "结算单位")
    private String unit2;

    @Schema(description = "单位重量（kg）")
    private BigDecimal unitWeight;

    @Schema(description = "保质天数")
    private Integer qualityDays;

    @Schema(description = "季节因子")
    private String seasonFactor;

    @Schema(description = "储存要求")
    private String storageReq;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态：ACTIVE-启用 INACTIVE-停用 DELETED-删除")
    private String status;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "修改人")
    private String updatedBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;
}
