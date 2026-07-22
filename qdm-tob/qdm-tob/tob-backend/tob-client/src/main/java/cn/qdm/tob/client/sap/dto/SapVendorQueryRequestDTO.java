package cn.qdm.tob.client.sap.dto;

import cn.qdm.tob.framework.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * SAP供应商查询请求实体
 */
@Data
public class SapVendorQueryRequestDTO {
    /**
     * 供应商编码
     */
    @JsonProperty("venderid")
    private String id;

    /**
     * 供应商类型
     */
    @JsonProperty("partnertype")
    private String type;

    /**
     * 供应商分组
     */
    @JsonProperty("vendergroup")
    private String group;

    /**
     * 业务模式
     */
    @JsonProperty("businessmodel")
    private String businessMode;

    /**
     * 增量日期
     */
    @JsonFormat(pattern = Constants.DEFAULT_DATE_PATTERN, timezone = Constants.DEFAULT_TIMEZONE)
    private LocalDate date;

    /**
     * 外围系统
     */
    private String system;
}
