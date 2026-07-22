package cn.qdm.tob.infrastructure.security.util;

import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

/**
 * 安全工具类 —— 提供静态方法获取当前登录用户信息
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * 获取当前 Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户，可能为 null（未认证时）
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = getAuthentication();
        if (Objects.isNull(authentication)) return null;

        Object principal = authentication.getPrincipal();
        return principal instanceof UserPrincipal up ? up : null;
    }

    /**
     * 获取当前登录用户，未认证时抛出 401
     */
    public static UserPrincipal requireCurrentUser() {
        UserPrincipal user = getCurrentUser();
        AssertUtils.notNull(user, ErrorCode.UNAUTHORIZED);
        return user;
    }

    /**
     * 获取当前登录用户 ID，可能为 null
     */
    public static Long getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return Objects.nonNull(user) ? user.getUserId() : null;
    }

    /**
     * 检查当前是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}
