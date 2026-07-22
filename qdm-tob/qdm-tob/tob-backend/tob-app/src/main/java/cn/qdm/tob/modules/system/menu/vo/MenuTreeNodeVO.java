package cn.qdm.tob.modules.system.menu.vo;

import cn.qdm.tob.framework.description.Description;
import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.modules.system.menu.enums.MenuType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "菜单树节点")
public class MenuTreeNodeVO {
    @Schema(description = "菜单编码")
    private Long id;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "上级菜单编码", hidden = true)
    private Long parentId;

    @Schema(description = "菜单类型")
    @Description
    private MenuType type;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "前端组件路径")
    private String component;

    @Schema(description = "名称图标")
    private String icon;

    @Schema(description = "状态")
    private RecordStatus status;

    @Schema(description = "子节点列表")
    private List<MenuTreeNodeVO> children;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "权限码列表（按钮专属）")
    private List<String> permissionCodes;
}
