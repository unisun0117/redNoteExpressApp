package cn.qdm.tob.modules.product.catalog.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品资料主表（prd_product），条码反查的数据来源
 */
@Data
@TableName("prd_product")
public class PrdProduct {

    /** 商品ID（自增主键） */
    @TableId
    private Long productId;

    /** 商品编码（SAP业务系统编号） */
    private String barcode;

    /** 商品名称 */
    private String name;

    /** SPU 编码 */
    private String spu;

    /** 小类编号（关联 prd_category.id） */
    private String categoryId;

    /** 规格 */
    private String spec;

    /** 产地 */
    private String originPlace;

    /** 品牌 */
    private String brand;

    /** 订购单位 */
    private String unit1;

    /** 结算单位 */
    private String unit2;

    /** 单位重量（kg） */
    private BigDecimal unitWeight;

    /** 保质天数 */
    private Integer qualityDays;

    /** 季节因子 */
    private String seasonFactor;

    /** 储存要求 */
    private String storageReq;

    /** 备注 */
    private String remark;

    /** 状态：ACTIVE / INACTIVE / DELETED */
    private String status;

    /** 创建人 */
    private String createdBy;

    /** 修改人 */
    private String updatedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 修改时间 */
    private LocalDateTime updatedAt;
}
