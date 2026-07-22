package cn.qdm.tob.client.wms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author zhaoxiaoyun
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WmsResponseBaseDTO<T> {
    @JsonProperty("Code")
    private Integer code;

    @JsonProperty("Status")
    private Integer status;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("Success")
    private Boolean success;

    private T data;

    @JsonProperty("Object")
    private T object;
}
