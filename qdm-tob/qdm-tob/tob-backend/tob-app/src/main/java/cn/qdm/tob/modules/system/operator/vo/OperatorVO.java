package cn.qdm.tob.modules.system.operator.vo;

import cn.qdm.tob.modules.system.operator.enums.OperatorStatus;
import cn.qdm.tob.modules.system.operator.enums.OperatorType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运营人员视图（列表/详情）
 */
@Data
@Schema(description = "运营人员视图")
public class OperatorVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "工号")
    private String employeeCode;

    @Schema(description = "登录账号（企微号）")
    private String loginId;

    @Schema(description = "用户名")
    private String realName;

    @Schema(description = "手机号（脱敏）")
    private String mobile;

    @Schema(description = "操作员类型")
    private OperatorType type;

    @Schema(description = "状态")
    private OperatorStatus status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;
}
