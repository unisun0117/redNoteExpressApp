package cn.qdm.tob.modules.system.role.vo;

import cn.qdm.tob.framework.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色分页查询参数")
public class RoleQueryVO extends PageQuery {

    @Schema(description = "角色编码（模糊匹配）")
    private String code;

    @Schema(description = "角色名称（模糊匹配）")
    private String name;
}
