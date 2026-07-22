package cn.qdm.tob.modules.system.menu.dto;

import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.modules.system.menu.enums.MenuGroup;
import cn.qdm.tob.modules.system.menu.enums.MenuType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "菜单创建请求")
public class MenuSaveDTO {
    @Schema(description = "父节点ID", defaultValue = "0")
    private Long parentId = 0L;

    @Schema(description = "编码（全局唯一）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "编码不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "仅允许字母、数字、下划线")
    private String code;

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "菜单类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "类型不能为空")
    private MenuType type;

    @Schema(description = "所属端", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "所属端不能为空")
    private MenuGroup group;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "前端组件路径")
    private String component;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序号", defaultValue = "0")
    private Integer sort = 0;

    @Schema(description = "状态", defaultValue = "ACTIVE")
    private RecordStatus status = RecordStatus.ACTIVE;

    /** 按钮专属：绑定的权限码列表 */
    @Schema(description = "权限码列表（按钮专属）")
    private List<String> permissionCodes;
}
