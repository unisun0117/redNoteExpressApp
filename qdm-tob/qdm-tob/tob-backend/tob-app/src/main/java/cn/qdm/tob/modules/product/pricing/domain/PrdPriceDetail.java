package cn.qdm.tob.modules.product.pricing.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格组明细实体
 * <p>
 * 联合主键：销售大区 + 价格组编码 + 商品条码
 */
@Data
@TableName("prd_price_detail")
public class PrdPriceDetail {

    @TableId
    private Long id;

    /** 销售大区编号 */
    private String salesRegionCode;

    /** 价格组编码 */
    private String priceGroupCode;

    /** 商品条码 */
    private String productBarcode;

    /** 商品名称（条码反查） */
    private String productName;

    /** 售价（元） */
    private BigDecimal price;

    /** 变动原因 */
    private String changeReason;

    /** 审批状态 */
    private ApprovalStatus approvalStatus;

    /** 待审批的新价格 */
    private BigDecimal pendingPrice;

    /** 创建人 */
    private String createdBy;

    /** 更新人 */
    private String updatedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
