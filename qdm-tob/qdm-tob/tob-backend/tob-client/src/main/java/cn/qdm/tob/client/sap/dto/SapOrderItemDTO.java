package cn.qdm.tob.client.sap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zhaoxiaoyun
 */
@Data
public class SapOrderItemDTO {
    private static final String SALES_MODE = "60";

    /**
     * 行号
     */
    @JsonProperty("order_item")
    private String orderItem;

    /**
     * 发货DC仓
     */
    @JsonProperty("dc_id")
    private String dcId;

    /**
     * 销售EAN
     */
    @JsonProperty("sku_code")
    private String skuCode;

    /**
     * 销售单位
     */
    @JsonProperty("sku_unit")
    private String skuUnit;

    /**
     * 销售计划数量
     */
    @JsonProperty("sku_qty")
    private BigDecimal skuQty;

    /**
     * 原销售金额
     */
    @JsonProperty("normal_sales_amt")
    private BigDecimal normalSalesAmt;

    /**
     * 原销售单价
     */
    @JsonProperty("normal_price")
    private BigDecimal normalPrice;

    /**
     * 折扣金额
     */
    @JsonProperty("off_amt")
    private BigDecimal offAmt;

    /**
     * 优惠金额
     */
    @JsonProperty("discount_amt")
    private BigDecimal discountAmt;

    /**
     * 促销扣减金额
     */
    @JsonProperty("promote_sales_amt")
    private BigDecimal promoteSalesAmt;

    /**
     * 结算金额
     */
    private BigDecimal money;

    /**
     * 结算单价
     */
    private BigDecimal price;

    /**
     * 销售币别
     */
    private String currency;

    /**
     * 是否增品(0=正常,1=赠品)
     */
    @JsonProperty("is_gift")
    private Integer isGift;

    /**
     * 删除标记(0=正, 1=删除)
     */
    @JsonProperty("is_del")
    private Integer isDel;

    /**
     * 退货原因编码
     */
    @JsonProperty("return_reason")
    private String returnReason;

    /**
     * 退货原因描述
     */
    @JsonProperty("return_reason_txt")
    private String returnReasonTxt;

    /**
     * 退货分类编码(SAP码表)
     */
    @JsonProperty("return_type")
    private String returnType;

    /**
     * 销售方式 - 目前统一为 60
     */
    @JsonProperty("sales_mode")
    private String salesMode = "60";
}
