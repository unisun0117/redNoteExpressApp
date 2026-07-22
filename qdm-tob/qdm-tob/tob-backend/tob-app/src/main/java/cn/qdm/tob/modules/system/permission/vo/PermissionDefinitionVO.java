package cn.qdm.tob.modules.system.permission.vo;

import cn.qdm.tob.framework.description.Description;
import cn.qdm.tob.infrastructure.security.Permissions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限定义 VO（对应 Permissions 枚举）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionDefinitionVO {
    @Description
    private Permissions authority;
}
