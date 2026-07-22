package cn.qdm.tob.client.wms.dto;

import cn.qdm.tob.framework.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author zhaoxiaoyun
 * @date 2022/12/12
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WmsOrderResponseDTO {
    @JsonProperty("wms_sheetid")
    private String wmsOrderNo;

    @JsonProperty("b_sheetid")
    private String tobOrderNo;

    @JsonProperty("orderdate")
    @JsonFormat(pattern = Constants.DEFAULT_DATE_PATTERN, timezone = Constants.DEFAULT_TIMEZONE)
    private LocalDate orderDate;
}
