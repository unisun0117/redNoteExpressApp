package cn.qdm.tob.client.message.dto;

import lombok.Data;

import java.util.List;

/**
 * @author zhaoxiaoyun
 */
@Data
public class SmsRequestDTO {
    /**
     * 应用，例如wms
     */
    private String app;
    /**
     * MD5加密后的签名
     */
    private String sign;
    /**
     * 时间戳
     */
    private Long timestamp;
    /**
     * 发送验证码的手机号数组
     */
    private String phone;
    /**
     * 短信内容
     */
    private String content;
}
