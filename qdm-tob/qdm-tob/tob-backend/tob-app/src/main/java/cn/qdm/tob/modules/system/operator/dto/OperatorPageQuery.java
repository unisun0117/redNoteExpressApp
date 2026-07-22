package cn.qdm.tob.modules.system.operator.dto;

import cn.qdm.tob.framework.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运营人员分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "运营人员分页查询参数")
public class OperatorPageQuery extends PageQuery {

    @Schema(description = "工号（模糊匹配）")
    private String employeeCode;

    @Schema(description = "用户名（模糊匹配）")
    private String realName;

    @Schema(description = "手机号（模糊匹配）")
    private String mobile;

    @Schema(description = "状态: ACTIVE/INACTIVE/LOCKED")
    private String status;

    @Schema(description = "类型: ADMIN/SALESMAN")
    private String type;
}
