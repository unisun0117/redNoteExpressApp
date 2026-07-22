package cn.qdm.tob.infrastructure.security;

import java.lang.annotation.*;

/**
 * 方法/类级权限检查注解，替代冗长的 {@code @PreAuthorize("hasAuthority(T(...)))}。
 *
 * <p>支持方法级和类级，类级注解对类内所有方法生效，方法级优先级更高。</p>
 *
 * <pre>{@code
 * // 方法级
 * @RequirePermission(SysPermissions.MENU_VIEW)
 * @GetMapping
 * public ResponseResult<?> view(@RequestParam Long id) { ... }
 *
 * // 类级（所有方法共享同一权限）
 * @RequirePermission(SysPermissions.DICT_VIEW)
 * public class DictController { ... }
 *
 * // 多权限 AND 逻辑
 * @RequirePermission({SysPermissions.USER_VIEW, SysPermissions.USER_EDIT})
 * }</pre>
 *
 * @see RequirePermissionAuthorizationManager
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /** 所需权限码，多个权限为 AND 逻辑（需全部满足） */
    Permissions[] value();
}
