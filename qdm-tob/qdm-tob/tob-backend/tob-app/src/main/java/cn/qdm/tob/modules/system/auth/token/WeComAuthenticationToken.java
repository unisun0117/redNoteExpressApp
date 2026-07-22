package cn.qdm.tob.modules.system.auth.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 企业微信认证 Token
 * 认证前：principal=企业微信 code
 * 认证后：principal=UserPrincipal（含 Authorities）
 */
public class WeComAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private Object credentials;

    public WeComAuthenticationToken(String code) {
        super((Collection<? extends GrantedAuthority>) null);
        this.principal = code;
        this.credentials = code;
        setAuthenticated(false);
    }

    public WeComAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
