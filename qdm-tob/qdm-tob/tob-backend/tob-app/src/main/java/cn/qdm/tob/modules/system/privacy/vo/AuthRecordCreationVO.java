package cn.qdm.tob.modules.system.privacy.vo;

import cn.qdm.tob.modules.system.privacy.enums.AuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 小程序端授权记录提交入参 VO
 */
@Data
@Schema(description = "提交授权记录")
public class AuthRecordCreationVO {

    @Schema(description = "授权类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "授权类型不能为空")
    private AuthType authType;

    @Schema(description = "授权时对应的文档版本号（隐私政策/摘要/用户规则/用户协议授权时传入）")
    private String version;
}
