package cn.qdm.tob.modules.system.auth.token;

import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * JWT 已认证 Token
 * 由 JwtAuthenticationFilter 从解析后的 JWT 构造，已认证状态。
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final UserPrincipal principal;

    public JwtAuthenticationToken(UserPrincipal principal,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
