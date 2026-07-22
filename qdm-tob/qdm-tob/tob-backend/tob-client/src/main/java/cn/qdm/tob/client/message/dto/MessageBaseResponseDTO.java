package cn.qdm.tob.client.message.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageBaseResponseDTO implements Serializable {
    @JsonProperty("Code")
    private Integer code;

    @JsonProperty("Status")
    private Integer status;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("Success")
    private Boolean success;
}
