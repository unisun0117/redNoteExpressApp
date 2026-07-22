package cn.qdm.tob.modules.system.role.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "角色编辑请求")
public class RoleEditVO {

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID")
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 60, message = "最长60字符")
    @Schema(description = "角色名称")
    private String name;

    @Size(max = 255, message = "最长255字符")
    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "绑定的菜单ID集合（全量覆盖）")
    private Set<Long> menuIds;
}
