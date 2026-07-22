package cn.qdm.tob.modules.order.sales.domain;

import cn.qdm.tob.modules.order.sales.enums.DiffStatus;
import cn.qdm.tob.modules.order.sales.enums.DiffType;
import cn.qdm.tob.modules.order.sales.enums.SapPushStatus;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 差补单表实体（1:1 扩展原订单）
 * <p>
 * 主键 orderId 即原订单 ord_sales_order.id，非自增
 */
@Data
@TableName("ord_order_diff")
public class OrdOrderDiff {

    /** 原订单ID（即 ord_sales_order.id，一一对应） */
    @TableId
    private Long orderId;

    /** 原订单编号（冗余，方便展示） */
    private String orderNo;

    /** 差补状态 */
    private DiffStatus diffStatus;

    /** 差补类型 */
    private DiffType diffType;

    /** 差补总金额 */
    private BigDecimal diffAmount;

    /** 补款支付流水号（仅补款+微信支付场景） */
    private String payTransactionId;

    /** 补款支付时间 */
    private LocalDateTime payTime;

    /** SAP推送状态 */
    private SapPushStatus sapPushStatus;

    /** 推送SAP时间 */
    private LocalDateTime sapPushTime;

    /** SAP回执时间 */
    private LocalDateTime sapPickingTime;

    /** 差补原因 */
    private String reason;

    /** 创建人（管理员） */
    private String createdBy;

    /** 修改人 */
    private String updatedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
