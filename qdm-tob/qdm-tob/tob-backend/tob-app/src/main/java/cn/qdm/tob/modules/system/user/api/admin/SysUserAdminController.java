package cn.qdm.tob.modules.system.user.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.system.user.enums.UserSource;
import cn.qdm.tob.modules.system.user.enums.UserStatus;
import cn.qdm.tob.modules.system.user.service.SysUserService;
import cn.qdm.tob.modules.system.user.vo.SysUserCreationVO;
import cn.qdm.tob.modules.system.user.vo.SysUserEditVO;
import cn.qdm.tob.modules.system.user.vo.SysUserSummaryVO;
import cn.qdm.tob.modules.system.user.vo.SysUserViewVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 客户账号管理 API（PC 后台）
 * <p>
 * 对 sys_user 表做后台 CRUD。新增免短信验证码（source=ADMIN、wechat_openid=NULL）。
 * 权限注册延期：见 PRD §7（待 RBAC 角色种子数据落地后统一补齐）。
 */
@RestController
@RequestMapping("/api/admin/system/users")
@RequiredArgsConstructor
public class SysUserAdminController {

    private final SysUserService sysUserService;

    @GetMapping("/page")
    @Operation(summary = "分页查询客户账号")
    public ResponseResult<Page<SysUserSummaryVO>> page(
            @Parameter(description = "页码") @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "姓名或手机号模糊搜索") @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "注册来源") @RequestParam(name = "source", required = false) UserSource source,
            @Parameter(description = "账号状态") @RequestParam(name = "status", required = false) UserStatus status) {
        return ResponseResult.success(
                sysUserService.page(pageNum, pageSize, keyword, source, status));
    }

    @GetMapping("/detail")
    @Operation(summary = "查询客户账号详情")
    public ResponseResult<SysUserViewVO> detail(
            @Parameter(description = "用户ID", required = true) @RequestParam(name = "id") Long id) {
        return ResponseResult.success(sysUserService.getDetail(id));
    }

    @PostMapping("/create")
    @Operation(summary = "后台新增客户账号（免短信验证码）")
    public ResponseResult<?> create(@Valid @RequestBody SysUserCreationVO vo) {
        sysUserService.create(vo);
        return ResponseResult.success();
    }

    @PutMapping("/edit")
    @Operation(summary = "编辑客户账号（仅姓名可改）")
    public ResponseResult<?> edit(@Valid @RequestBody SysUserEditVO vo) {
        sysUserService.edit(vo);
        return ResponseResult.success();
    }

    @PutMapping("/toggle-status")
    @Operation(summary = "切换账号状态（ACTIVE↔INACTIVE）")
    public ResponseResult<?> toggleStatus(
            @Parameter(description = "用户ID", required = true) @RequestParam(name = "id") Long id) {
        sysUserService.toggleStatus(id);
        return ResponseResult.success();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "硬删除客户账号（释放手机号）")
    public ResponseResult<?> delete(
            @Parameter(description = "用户ID", required = true) @RequestParam(name = "id") Long id) {
        sysUserService.delete(id);
        return ResponseResult.success();
    }
}
