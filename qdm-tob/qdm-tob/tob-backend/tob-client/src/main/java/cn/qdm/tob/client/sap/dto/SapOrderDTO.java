package cn.qdm.tob.client.sap.dto;

import cn.qdm.tob.framework.Constants;
import cn.qdm.tob.client.sap.enums.DeliveryType;
import cn.qdm.tob.client.sap.enums.OrderChannel;
import cn.qdm.tob.client.sap.enums.OrderType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * @author zhaoxiaoyun
 */
@Data
public class SapOrderDTO {
    /**
     * 内部订单编号
     */
    @JsonProperty("inner_order_id")
    private String innerOrderId;
    /**
     * 外部订单编号
     */
    @JsonProperty("outer_order_id")
    private String outerOrderId;
    /**
     * 原内部订单号
     */
    @JsonProperty("origin_inner_order_id")
    private String originInnerOrderId;
    /**
     * 原外部订单号
     */
    @JsonProperty("origin_outer_order_id")
    private String originOuterOrderId;
    /**
     * 订单类型(1=订货单,2=退货单,3=紧急加单,4=差补单,5=线下尾货单)
     */
    @JsonProperty("order_type")
    private OrderType orderType;
    /**
     * 客户编号
     */
    @JsonProperty("customer_id")
    private String customerId;
    /**
     * 下单日期
     */
    @JsonProperty("create_date")
    @JsonFormat(pattern = Constants.PLAIN_DATE_PATTERN, timezone = Constants.DEFAULT_TIMEZONE)
    private LocalDate createDate;
    /**
     * 支付日期
     */
    @JsonProperty("pay_date")
    @JsonFormat(pattern = Constants.PLAIN_DATE_PATTERN, timezone = Constants.DEFAULT_TIMEZONE)
    private LocalDate payDate;
    /**
     * 支付时间
     */
    @JsonProperty("pay_time")
    @JsonFormat(pattern = Constants.DEFAULT_TIME_PATTERN, timezone = Constants.DEFAULT_TIMEZONE)
    private LocalTime payTime;
    /**
     * 支付信息
     */
    @JsonProperty("pay_types")
    private List<SapOrderPaymentDTO> payments;
    /**
     * 到店日期
     */
    @JsonProperty("arrive_date")
    @JsonFormat(pattern = Constants.PLAIN_DATE_PATTERN, timezone = Constants.DEFAULT_TIMEZONE)
    private LocalDate arriveDate;
    /**
     * 备注
     */
    private String remark;
    /**
     * A-b2b线上   B-尾货线上  C-尾货线下
     */
    @JsonProperty("bus_channel")
    private OrderChannel busChannel;
    /**
     * A：自提，B：送货上门
     */
    @JsonProperty("delivery_type")
    private DeliveryType deliveryType;
    /**
     * 物流费用
     */
    @JsonProperty("logistics_cost")
    private BigDecimal logisticsCost;
    /**
     * 订单明细
     */
    List<SapOrderItemDTO> items;
    /**
     * 备注1--传系统名称
     */
    private String arg1;

    /**
     * 结算客户编码
     */
    @JsonProperty("settled_customer")
    private String settlementCustomNo;
}
