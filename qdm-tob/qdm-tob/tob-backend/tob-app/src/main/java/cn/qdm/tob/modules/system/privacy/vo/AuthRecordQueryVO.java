package cn.qdm.tob.modules.system.privacy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 隐私授权记录分页查询入参 VO
 */
@Data
@Schema(description = "隐私授权记录查询条件")
public class AuthRecordQueryVO {

    @Schema(description = "小程序用户 openid（模糊，可选）")
    private String openid;

    @Schema(description = "手机号（模糊，可选）")
    private String phone;

    @Schema(description = "授权类型（枚举名，可选）")
    private String authType;

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", defaultValue = "20")
    private Integer pageSize = 20;
}
