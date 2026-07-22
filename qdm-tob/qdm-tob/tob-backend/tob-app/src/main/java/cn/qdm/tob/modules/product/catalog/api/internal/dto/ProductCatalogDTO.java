package cn.qdm.tob.modules.product.catalog.api.internal.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品资料 DTO，供订单模块跨模块查询
 */
@Data
public class ProductCatalogDTO {

    /** 商品条码 */
    private String productBarcode;

    /** 商品名称 */
    private String productName;

    /** 销售大区编号 */
    private String salesRegionCode;

    /** 状态：LISTED=已上架 UNLISTED=已下架 */
    private String status;

    /** 小程序名称（商品别名） */
    private String miniappName;

    /** 商品主图 URL */
    private String mainImage;

    /** 订购基数 */
    private BigDecimal orderBaseQty;

    /** 订购下限 */
    private BigDecimal orderMinQty;

    /** 订购上限 */
    private BigDecimal orderMaxQty;

    /** 今日可用数量 */
    private BigDecimal dailyAvailable;

    /** 仓库编码（SAP） */
    private String warehouseCode;

    /** 仓库名称 */
    private String warehouseName;

    /** 售价（来自价格表 INNER JOIN） */
    private BigDecimal price;
}
