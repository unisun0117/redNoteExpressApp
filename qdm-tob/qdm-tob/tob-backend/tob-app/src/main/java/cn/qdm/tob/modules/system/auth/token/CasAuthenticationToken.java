package cn.qdm.tob.modules.system.auth.token;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * CAS 认证 Token
 * 认证前：principal=ticket，details 中携带 service URL
 * 认证后：principal=UserPrincipal（含 Authorities）
 */
@Getter
public class CasAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;
    private Object credentials;
    /** CAS service URL（验证 ticket 时必须与获取 ticket 时一致） */
    private final String service;

    public CasAuthenticationToken(String ticket, String service) {
        super((Collection<? extends GrantedAuthority>) null);
        this.principal = ticket;
        this.credentials = ticket;
        this.service = service;
        setAuthenticated(false);
    }

    public CasAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        this.service = null;
        super.setAuthenticated(true);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
