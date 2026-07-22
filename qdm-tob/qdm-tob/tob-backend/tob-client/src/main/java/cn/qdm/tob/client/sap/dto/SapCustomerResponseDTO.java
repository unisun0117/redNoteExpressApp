package cn.qdm.tob.client.sap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author zhaoxiaoyun
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SapCustomerResponseDTO {
    @JsonProperty("RESULT")
    private String result;

    @JsonProperty("MESSAGE")
    private String message;

    @JsonProperty("KUNNR")
    private String no;
}
