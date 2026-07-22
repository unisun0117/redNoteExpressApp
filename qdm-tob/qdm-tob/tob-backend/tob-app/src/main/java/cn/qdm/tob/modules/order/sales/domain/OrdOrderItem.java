package cn.qdm.tob.modules.order.sales.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单商品明细表实体
 * <p>
 * 差补时仅 UPDATE actualQty / diffQty，不新增行
 */
@Data
@TableName("ord_order_item")
public class OrdOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID（关联 ord_sales_order.id） */
    private Long orderId;

    /** 配送仓库ID（行级指定） */
    private Long warehouseId;

    /** 商品SKU ID */
    private String skuId;

    /** 商品条码（快照） */
    private String barcode;

    /** 商品名称（快照） */
    private String goodsName;

    /** 结算单位（快照，如千克/箱） */
    private String unit;

    /** 订购数量 */
    private BigDecimal quantity;

    /** 实际出库数量（SAP回写后更新） */
    private BigDecimal actualQty;

    /** 差异数量（=actualQty - quantity，≠0触发差补） */
    private BigDecimal diffQty;

    /** 商品单价（快照） */
    private BigDecimal unitPrice;

    /** 行级商品总金额（=quantity * unitPrice） */
    private BigDecimal goodsTotal;

    /** 促销分摊金额 */
    private BigDecimal promotionShare;

    /** 优惠券分摊金额 */
    private BigDecimal couponShare;

    /** 折扣分摊金额 */
    private BigDecimal discountShare;

    /** 行级实付金额 */
    private BigDecimal goodsPaid;

    /** 附加服务 */
    private String extraService;

    /** 创建人 */
    private String createdBy;

    /** 修改人 */
    private String updatedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
