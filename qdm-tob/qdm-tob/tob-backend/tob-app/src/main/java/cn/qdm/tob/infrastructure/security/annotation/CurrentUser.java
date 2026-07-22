package cn.qdm.tob.infrastructure.security.annotation;

import java.lang.annotation.*;

/**
 * 注入当前登录用户
 * <p>
 * 标注在 Controller 方法的 {@link cn.qdm.tob.modules.system.auth.domain.UserPrincipal UserPrincipal}
 * 参数上，自动从 {@code SecurityContextHolder} 注入当前认证用户信息。
 * </p>
 *
 * <pre>{@code
 * @GetMapping("/profile")
 * public ResponseResult<UserVO> getProfile(@CurrentUser UserPrincipal currentUser) {
 *     Long userId = currentUser.getUserId();
 *     // ...
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
