package cn.qdm.tob.modules.system.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 后台新增客户账号入参 VO
 * <p>
 * 后台新增免短信验证码：wechat_openid 置空，source=ADMIN，status=ACTIVE。
 */
@Data
@Schema(description = "新增客户账号")
public class SysUserCreationVO {

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
    private String realName;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的11位手机号")
    private String mobile;
}
