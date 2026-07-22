package cn.qdm.tob.modules.system.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信验证码验证请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsVerifyVO {
    private String phone;
    private String smsCode;
}
