package cn.qdm.tob.modules.system.auth.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 小程序微信认证 Token
 * 认证前：principal=getPhoneNumber 返回的 phoneCode，credentials=wx.login() 返回的 wxCode
 * 认证后：principal=UserPrincipal
 */
public class WechatAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private Object credentials;

    /**
     * 双 code 构造（完整微信登录流程）
     * @param phoneCode getPhoneNumber 返回的动态令牌，用于解密手机号
     * @param wxCode    wx.login() 返回的临时 code，用于 code2Session 换取 openId
     */
    public WechatAuthenticationToken(String phoneCode, String wxCode) {
        super((Collection<? extends GrantedAuthority>) null);
        this.principal = phoneCode;
        this.credentials = wxCode;
        setAuthenticated(false);
    }

    public WechatAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
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
