package cn.qdm.tob.modules.system.role.vo;

import cn.qdm.tob.framework.model.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "角色创建请求")
public class RoleCreationVO {

    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "仅允许字母、数字、下划线")
    @Size(max = 60, message = "最长60字符")
    @Schema(description = "角色编码", example = "WAREHOUSE_ADMIN")
    private String code;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 60, message = "最长60字符")
    @Schema(description = "角色名称", example = "仓库管理员")
    private String name;

    @Size(max = 255, message = "最长255字符")
    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态", defaultValue = "ACTIVE")
    private RecordStatus status;

    @Schema(description = "绑定的菜单ID集合（全量覆盖）")
    private Set<Long> menuIds;
}
