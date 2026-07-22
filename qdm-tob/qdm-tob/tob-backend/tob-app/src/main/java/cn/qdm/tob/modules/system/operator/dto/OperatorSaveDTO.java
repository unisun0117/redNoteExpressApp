package cn.qdm.tob.modules.system.operator.dto;

import cn.qdm.tob.modules.system.operator.enums.OperatorStatus;
import cn.qdm.tob.modules.system.operator.enums.OperatorType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 运营人员保存请求（创建/更新复用）
 */
@Data
@Schema(description = "运营人员保存请求")
public class OperatorSaveDTO {

    @Schema(description = "工号（创建时必填，更新时忽略）")
    private String employeeCode;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String realName;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的11位手机号")
    private String mobile;

    @Schema(description = "操作员类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private OperatorType type;

    @Schema(description = "状态", defaultValue = "ACTIVE")
    private OperatorStatus status;
}
