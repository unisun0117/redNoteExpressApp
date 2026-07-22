package cn.qdm.tob.modules.system.menu.api.admin;


import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.infrastructure.security.annotation.CurrentUser;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.modules.system.menu.dto.MenuEditDTO;
import cn.qdm.tob.modules.system.menu.dto.MenuSaveDTO;
import cn.qdm.tob.modules.system.menu.enums.MenuGroup;
import cn.qdm.tob.modules.system.menu.service.SysMenuService;
import cn.qdm.tob.modules.system.menu.vo.MenuTreeNodeVO;
import cn.qdm.tob.modules.system.menu.vo.MenuViewVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Web 管理端 — 菜单管理 API（CRUD + 管理端树 + 按钮列表 + 权限选项）
 */
@RestController
@RequestMapping("/api/admin/menu")
@RequiredArgsConstructor
public class MenuAdminController {
    private final SysMenuService menuService;

    @GetMapping("/view")
    @Operation(summary = "获取菜单详情")
    @RequirePermission(Permissions.MENU_VIEW)
    public ResponseResult<MenuViewVO> view(@RequestParam Long id) {
        return ResponseResult.success(menuService.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "创建菜单")
    @RequirePermission(Permissions.MENU_EDIT)
    public ResponseResult<?> create(@Valid @RequestBody MenuSaveDTO dto) {
        menuService.create(dto);
        return ResponseResult.success();
    }

    @PostMapping("/update")
    @Operation(summary = "更新菜单")
    @RequirePermission(Permissions.MENU_EDIT)
    public ResponseResult<?> update(@Valid @RequestBody MenuEditDTO dto) {
        menuService.update(dto);
        return ResponseResult.success();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除菜单")
    @RequirePermission(Permissions.MENU_DELETE)
    public ResponseResult<?> delete(@RequestParam Long id) {
        menuService.delete(id);
        return ResponseResult.success();
    }

    @GetMapping("/tree")
    @Operation(summary = "管理端菜单树（所有状态，不含按钮 → 菜单管理页；含按钮 → 角色管理页）")
    @RequirePermission(Permissions.MENU_VIEW)
    public ResponseResult<List<MenuTreeNodeVO>> getAdminTree(@RequestParam MenuGroup group,
                                                             @RequestParam(defaultValue = "false") boolean includeButtons) {
        return ResponseResult.success(menuService.getAdminTree(group, includeButtons));
    }

    @GetMapping("/buttons")
    @Operation(summary = "指定页面下的按钮列表")
    @RequirePermission(Permissions.MENU_VIEW)
    public ResponseResult<List<MenuTreeNodeVO>> getButtons(@RequestParam Long pageId) {
        return ResponseResult.success(menuService.getButtons(pageId));
    }

    @GetMapping("/userTree")
    @Operation(summary = "获取当前用户的菜单树（管理后台侧边栏用）")
    public ResponseResult<List<MenuTreeNodeVO>> getUserTree(@RequestParam MenuGroup group,
                                                             @CurrentUser UserPrincipal currentUser) {
        return ResponseResult.success(menuService.getUserTree(group, currentUser.getUserId()));
    }

}
