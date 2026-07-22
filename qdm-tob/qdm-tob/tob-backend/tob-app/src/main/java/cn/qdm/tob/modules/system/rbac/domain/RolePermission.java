package cn.qdm.tob.modules.system.rbac.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色-权限关系实体（旧链路 sys_role_permission 表）
 *
 * @deprecated 角色授权已迁移至 {@code sys_role_menu}（角色 → 菜单 → 权限码），
 *             见 {@link cn.qdm.tob.modules.system.rbac.service.OperatorAuthorityLoader}。
 *             本类及相关链路保留供后续按需启用。
 */
@Deprecated
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_role_permission")
public class RolePermission {
    private Long roleId;

    private Long permissionId;

    private LocalDateTime createdAt;
}
