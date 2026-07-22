package cn.qdm.tob.modules.system.role.api.internal.dto;

import lombok.Data;

/**
 * 角色精简 DTO（id + name），供跨模块调用。
 * <p>调用方通过 {@link cn.qdm.tob.modules.system.role.service.SysRoleService#listAll()} 获取。</p>
 */
@Data
public class RoleSimpleDTO {
    /** 角色 ID */
    private Long id;
    /** 角色名称 */
    private String name;
}
