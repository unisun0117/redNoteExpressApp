package cn.qdm.tob.modules.system.privacy.vo;

import cn.qdm.tob.framework.description.Description;
import cn.qdm.tob.modules.system.privacy.enums.AuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 隐私授权记录出参 VO
 */
@Data
@Schema(description = "隐私授权记录")
public class AuthRecordViewVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "小程序用户 openid")
    private String openid;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "授权类型")
    @Description
    private AuthType authType;

    @Schema(description = "授权时对应的文档版本号")
    private String version;

    @Schema(description = "授权时间")
    private LocalDateTime authTime;
}
