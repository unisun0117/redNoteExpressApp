package cn.qdm.tob.modules.system.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台编辑客户账号入参 VO
 * <p>
 * 仅允许修改姓名；手机号不可改（编辑时置灰）。
 */
@Data
@Schema(description = "编辑客户账号")
public class SysUserEditVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
    private String realName;
}
