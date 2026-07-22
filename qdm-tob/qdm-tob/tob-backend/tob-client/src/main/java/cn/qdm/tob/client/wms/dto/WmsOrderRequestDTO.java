package cn.qdm.tob.client.wms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author zhaoxiaoyun
 */
@Data
public class WmsOrderRequestDTO {
    public WmsOrderRequestDTO() {
        this.userId = "B2B";
    }

    public WmsOrderRequestDTO(List<WmsOrderDTO> data) {
        this();
        this.data = data;
    }

    @JsonProperty("userid")
    private String userId;

    private List<WmsOrderDTO> data;
}
