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
public class WeComMessageResponseDTO extends WeComResponseBaseDTO {
    @JsonProperty("invaliduser")
    private String invalidUser;

    @JsonProperty("invalidparty")
    private String invalidParty;

    @JsonProperty("invalidtag")
    private String invalidTag;
}
