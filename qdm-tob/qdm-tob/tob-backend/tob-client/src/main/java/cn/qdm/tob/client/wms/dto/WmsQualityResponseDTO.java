package cn.qdm.tob.client.wms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author zhaoxiaoyun
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WmsQualityResponseDTO {
    @JsonProperty(value = "photourl")
    private String photoUrl;

    private List<String> pics;

    @JsonProperty(value = "dcid")
    private String dcId;

    @JsonProperty(value = "showdate")
    private String showDate;
}
