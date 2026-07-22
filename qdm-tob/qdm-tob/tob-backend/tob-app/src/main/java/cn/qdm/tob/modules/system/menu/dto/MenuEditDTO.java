package cn.qdm.tob.modules.system.menu.dto;

import cn.qdm.tob.framework.model.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "菜单更新请求")
public class MenuEditDTO {
    @Schema(description = "菜单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "前端组件路径")
    private String component;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "状态")
    private RecordStatus status;

    /** 按钮专属：权限码列表（保存时全量覆盖） */
    @Schema(description = "权限码列表（按钮专属）")
    private List<String> permissionCodes;
}
