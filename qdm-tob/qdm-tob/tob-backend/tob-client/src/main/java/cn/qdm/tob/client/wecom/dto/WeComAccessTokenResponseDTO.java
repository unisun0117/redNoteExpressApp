package cn.qdm.tob.client.wecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业微信获取访问令牌响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeComAccessTokenResponseDTO {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("errcode")
    private Integer errcode;

    @JsonProperty("errmsg")
    private String errmsg;
}
