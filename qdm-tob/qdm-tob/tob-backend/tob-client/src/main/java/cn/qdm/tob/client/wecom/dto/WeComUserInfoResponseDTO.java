package cn.qdm.tob.client.wecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业微信获取用户信息响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WeComUserInfoResponseDTO extends WeComResponseBaseDTO {
    @JsonProperty("UserId")
    private String userId;

    @JsonProperty("DeviceId")
    private String deviceId;
}
