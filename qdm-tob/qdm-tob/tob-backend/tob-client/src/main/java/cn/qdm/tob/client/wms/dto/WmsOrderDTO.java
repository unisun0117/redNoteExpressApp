package cn.qdm.tob.client.wms.dto;

import cn.qdm.tob.framework.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * @author zhaoxiaoyun
 */
@Data
public class WmsOrderDTO {
    /**
     * 订单单号
     */
    @JsonProperty("sheetid")
    private String orderNo;
    /**
     * 仓库编码
     */
    @JsonProperty("dcid")
    private String dcId;

    /**
     * 订购日期
     */
    @JsonProperty("orderdate")
    @JsonFormat(pattern = Constants.DEFAULT_DATETIME_PATTERN, timezone = Constants.DEFAULT_TIMEZONE)
    private LocalDate orderDate;

    /**
     * 客户编码
     */
    @JsonProperty("shopid")
    private String customNo;

    /**
     *客户名称
     */
    @JsonProperty("factname")
    private String customName;

    /**
     * CN ： 国家编码
     */
    private String country = "CN";

    /**
     * 地址
     */
    private String city;

    private String memo;

    private Integer source;

    private List<WmsOrderItemDTO> detail;
}
