package cn.qdm.tob.client.wms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author zhaoxiaoyun
 */
@Data
public class WmsQualityRequestDTO {
    public WmsQualityRequestDTO(LocalDate date) {
        this.date = date;
    }

    /**
     * 查询时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("showdate")
    private LocalDate date;
}
