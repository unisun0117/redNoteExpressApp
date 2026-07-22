package cn.qdm.tob.modules.product.catalog.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 基础 SKU 表（商品条码基础数据，条码反查的数据来源）
 */
@Data
@TableName("prd_sku")
public class PrdSku {

    /** 商品条码（主键） */
    @TableId
    private String barcode;

    /** 条码名称 */
    private String name;

    /** 商品 SPU 编码 */
    private String spu;

    /** 条码单位 */
    private String unit;

    /** 条码规格 */
    private String spec;

    /** 转换比例 */
    private BigDecimal tranValue;

    /** 是否结算单位（1=是） */
    private Integer settlementFlag;

    /** 是否称重商品（1=称重 0=非称重） */
    private Integer weightFlag;

    /** 单位重量（千克） */
    private BigDecimal unitWeight;

    /** 状态：ACTIVE / INACTIVE */
    private String status;

    /** 备注 */
    private String memo;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
