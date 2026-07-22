package cn.qdm.tob.modules.system.menu.vo;

import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.modules.system.menu.enums.MenuGroup;
import cn.qdm.tob.modules.system.menu.enums.MenuType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "菜单")
public class MenuViewVO {
    @Schema(description = "菜单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "父级菜单编码", requiredMode = Schema.RequiredMode.AUTO, defaultValue = "0")
    private Long parentId;

    @Schema(description = "菜单组", requiredMode = Schema.RequiredMode.AUTO)
    private MenuGroup group;

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "菜单类型", requiredMode = Schema.RequiredMode.AUTO)
    private MenuType type;

    @Schema(description = "路由路径", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String path;

    @Schema(description = "前端组件路径", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String component;

    @Schema(description = "排序号", requiredMode = Schema.RequiredMode.NOT_REQUIRED, defaultValue = "0")
    private Integer sort;

    @Schema(description = "菜单状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED, defaultValue = "ACTIVE")
    private RecordStatus status;
}
