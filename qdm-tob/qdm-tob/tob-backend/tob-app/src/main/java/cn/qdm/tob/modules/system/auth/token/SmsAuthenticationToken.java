package cn.qdm.tob.modules.system.auth.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 小程序短信认证 Token
 * 认证前：principal=手机号，credentials=短信验证码
 * 认证后：principal=UserPrincipal，credentials=null
 */
public class SmsAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private Object credentials;

    /** 认证前：未认证 */
    public SmsAuthenticationToken(String mobile, String smsCode) {
        super((Collection<? extends GrantedAuthority>) null);
        this.principal = mobile;
        this.credentials = smsCode;
        setAuthenticated(false);
    }

    /** 认证后：已认证 */
    public SmsAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
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
