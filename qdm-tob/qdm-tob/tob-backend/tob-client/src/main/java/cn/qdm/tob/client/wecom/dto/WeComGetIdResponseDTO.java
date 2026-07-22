package cn.qdm.tob.client.wecom.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhaoxiaoyun
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeComGetIdResponseDTO extends WeComResponseBaseDTO {
    @JsonProperty("userid")
    private String userId;
}
