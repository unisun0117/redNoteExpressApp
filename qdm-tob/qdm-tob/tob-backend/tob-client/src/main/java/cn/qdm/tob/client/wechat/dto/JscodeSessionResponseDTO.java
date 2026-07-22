package cn.qdm.tob.client.wechat.dto;

import lombok.Data;
/**
 * 微信 jscode2session 接口响应
 */
@Data
public class JscodeSessionResponseDTO {
    private String openid;
    private String sessionKey;
    private Integer errcode;
    private String errmsg;
}
