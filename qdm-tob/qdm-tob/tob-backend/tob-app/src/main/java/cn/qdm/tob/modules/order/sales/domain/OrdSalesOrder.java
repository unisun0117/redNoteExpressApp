package cn.qdm.tob.modules.order.sales.domain;

import cn.qdm.tob.modules.order.sales.enums.DeliveryType;
import cn.qdm.tob.modules.order.sales.enums.OrderStatus;
import cn.qdm.tob.modules.order.sales.enums.PayMethod;
import cn.qdm.tob.modules.order.sales.enums.ReturnStatus;
import cn.qdm.tob.modules.order.sales.enums.SapPushStatus;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售订单主表实体
 */
@Data
@TableName("ord_sales_order")
public class OrdSalesOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号（唯一） */
    private String orderNo;

    /** 订单状态 */
    private OrderStatus orderStatus;

    /** 客户档案ID */
    private Long customerId;

    /** 销售大区ID */
    private Long salesRegionId;

    /** 销售员ID */
    private Long salesmanId;

    /** 下单人（小程序用户）ID */
    private Long submitUserId;

    /** 客户编码（快照） */
    private String customerCode;

    /** 客户名称（快照） */
    private String customerName;

    /** 客户类型（快照） */
    private String customerType;

    /** 下单人姓名（快照） */
    private String submitUserName;

    /** 下单手机号（快照） */
    private String submitUserPhone;

    /** 收货人姓名（快照） */
    private String receiverName;

    /** 收货人电话（快照） */
    private String receiverPhone;

    /** 收货地址（快照） */
    private String receiverAddress;

    /** 配送方式 */
    private DeliveryType deliveryType;

    /** 配送时间起 */
    private LocalDateTime deliveryStartTime;

    /** 配送时间止 */
    private LocalDateTime deliveryEndTime;

    /** 配送备注 */
    private String deliveryRemark;

    /** 下单时间 */
    private LocalDateTime orderTime;

    /** 期望到货日期 */
    private LocalDate arrivalDate;

    /** 取消时间 */
    private LocalDateTime cancelTime;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 商品总金额 */
    private BigDecimal goodsAmount;

    /** 促销金额 */
    private BigDecimal promotionAmount;

    /** 优惠券金额 */
    private BigDecimal couponAmount;

    /** 折扣金额 */
    private BigDecimal discountAmount;

    /** 商品实付金额 */
    private BigDecimal goodsPaidAmount;

    /** 运费 */
    private BigDecimal freightAmount;

    /** 实付总金额 */
    private BigDecimal paidAmount;

    /** 结算客户编码 */
    private String settleCustomerCode;

    /** 支付方式 */
    private PayMethod payMethod;

    /** 支付流水号 */
    private String payTransactionId;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 商户号 */
    private String merchantId;

    /** 商户号名称 */
    private String merchantName;

    /** SAP推送状态 */
    private SapPushStatus sapPushStatus;

    /** 推送SAP时间 */
    private LocalDateTime sapPushTime;

    /** SAP分拣出库单回执时间 */
    private LocalDateTime sapPickingTime;

    /** 退货状态 */
    private ReturnStatus returnStatus;

    /** 物流单号 */
    private String logisticsNo;

    /** 创建人 */
    private String createdBy;

    /** 修改人 */
    private String updatedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
