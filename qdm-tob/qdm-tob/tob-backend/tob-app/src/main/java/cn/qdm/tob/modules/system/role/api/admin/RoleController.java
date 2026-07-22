package cn.qdm.tob.modules.system.role.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.modules.system.role.api.internal.dto.RoleSimpleDTO;
import cn.qdm.tob.modules.system.role.service.SysRoleService;
import cn.qdm.tob.modules.system.role.vo.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 角色管理 API
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleService sysRoleService;

    @GetMapping("/search")
    @Operation(summary = "分页搜索角色列表")
    @RequirePermission(Permissions.ROLE_VIEW)
    public ResponseResult<IPage<RoleViewVO>> search(@Valid RoleQueryVO query) {
        return ResponseResult.success(sysRoleService.pageSearch(query));
    }

    @GetMapping("/view")
    @Operation(summary = "查看角色详情")
    @RequirePermission(Permissions.ROLE_VIEW)
    public ResponseResult<RoleViewVO> view(@RequestParam Long id) {
        return ResponseResult.success(sysRoleService.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "创建角色")
    @RequirePermission(Permissions.ROLE_EDIT)
    public ResponseResult<?> create(@Valid @RequestBody RoleCreationVO vo) {
        sysRoleService.create(vo);
        return ResponseResult.success();
    }

    @PostMapping("/update")
    @Operation(summary = "更新角色")
    @RequirePermission(Permissions.ROLE_EDIT)
    public ResponseResult<?> update(@Valid @RequestBody RoleEditVO vo) {
        sysRoleService.update(vo);
        return ResponseResult.success();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除角色")
    @RequirePermission(Permissions.ROLE_DELETE)
    public ResponseResult<?> delete(@RequestParam Long id) {
        sysRoleService.delete(id);
        return ResponseResult.success();
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有角色（id+name，供下拉/弹窗使用）")
    @RequirePermission(Permissions.ROLE_VIEW)
    public ResponseResult<List<RoleSimpleDTO>> all() {
        return ResponseResult.success(sysRoleService.listAll());
    }

    @GetMapping("/menus")
    @Operation(summary = "查询角色已绑定的菜单ID")
    @RequirePermission(Permissions.ROLE_VIEW)
    public ResponseResult<Set<Long>> getMenus(@RequestParam Long roleId) {
        return ResponseResult.success(sysRoleService.getMenuIds(roleId));
    }
}
