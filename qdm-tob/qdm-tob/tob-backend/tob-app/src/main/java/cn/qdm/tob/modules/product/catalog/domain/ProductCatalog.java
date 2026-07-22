package cn.qdm.tob.modules.product.catalog.domain;

import cn.qdm.tob.modules.product.catalog.enums.ProductStatus;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品资料实体（主键 = 销售大区 + 商品条码）
 */
@Data
@TableName("prd_product_catalog")
public class ProductCatalog {

    @TableId
    private Long id;

    /** 销售大区编号 */
    private String salesRegionCode;

    /** 销售大区名称（冗余） */
    private String salesRegionName;

    /** 商品条码 */
    private String productBarcode;

    /** 商品名称（条码反查） */
    private String productName;

    /** 仓库编码（SAP） */
    private String warehouseCode;

    /** 仓库名称（冗余） */
    private String warehouseName;

    /** 状态：LISTED=已上架 UNLISTED=已下架 */
    private ProductStatus status;

    /** 小程序名称（商品别名） */
    private String miniappName;

    /** 商品主图 URL */
    private String mainImage;

    /** 轮播图 URL 列表（JSON 数组，最多 9 张） */
    private String carouselImages;

    /** 商品详情（富文本） */
    private String productDetail;

    /** 订购基数 */
    private BigDecimal orderBaseQty;

    /** 订购下限 */
    private BigDecimal orderMinQty;

    /** 订购上限 */
    private BigDecimal orderMaxQty;

    /** 每日可用库存 */
    private BigDecimal dailyStock;

    /** 今日可用数量（=库存-已售） */
    private BigDecimal dailyAvailable;

    /** 今日已售数量（订单同步） */
    private BigDecimal dailySold;

    /** 创建人 */
    private String createdBy;

    /** 修改人 */
    private String updatedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 修改时间 */
    private LocalDateTime updatedAt;
}
