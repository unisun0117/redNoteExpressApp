package cn.qdm.tob.modules.system.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小程序注册请求入参（mall 端 VO，命名遵循 RULES.md *CreationVO 约定）
 * <p>
 * 客户首次注册：姓名 + 手机号 + 短信验证码，可选微信 openId。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterCreationVO {
    /** 姓名（落库 sys_user.real_name） */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 短信验证码 */
    private String smsCode;

    /** 微信 openId（可选，微信环境内注册时传入并绑定） */
    private String wechatOpenId;
}
