package cn.qdm.tob.client.wms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zhaoxiaoyun
 */
@Data
public class WmsOrderItemDTO {
    @JsonProperty("linenum")
    private String lineNum;

    private String barcode;

    @JsonProperty("giveflag")
    private Integer giveFlag;

    /**
     * 下单数量
     */
    @JsonProperty("order_qty")
    private BigDecimal orderQty;

    private BigDecimal price;

    private BigDecimal discount;

    /**
     * 产地
     */
    @JsonProperty("orgplace")
    private String orgPlace;

    /**
     * 单价
     */
    @JsonProperty("saleprice")
    private BigDecimal salePrice;

    /**
     * 折扣原因
     */
    @JsonProperty("discount_reason")
    private String discountReason;
}
