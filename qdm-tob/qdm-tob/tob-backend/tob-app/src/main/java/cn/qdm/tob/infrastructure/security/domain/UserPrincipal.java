package cn.qdm.tob.infrastructure.security.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * 用户主体信息
 * 实现 Spring Security 的 UserDetails，贯穿整个认证流程，最终序列化到 JWT Token 中。
 */
@Data
@JsonIgnoreProperties({"password", "username", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
public class UserPrincipal implements UserDetails, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 认证类型（miniprogram_sms | miniprogram_wechat | cas | wecom | signature） */
    private String authType;

    /** 手机号 */
    private String phone;

    /** 姓名 */
    private String name;

    /** 工号（仅 Admin 类认证有值） */
    private String employeeCode;

    /** JWT ID，用于黑名单和刷新，不对外暴露 */
    @JsonIgnore
    private String jti;

    /** 权限列表（仅 Admin 类认证加载，User 为空），不对外暴露 */
    @JsonIgnore
    private Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

    public UserPrincipal() {
    }

    public UserPrincipal(Long userId, String authType, String phone, String name) {
        this.userId = userId;
        this.authType = authType;
        this.phone = phone;
        this.name = name;
    }

    public UserPrincipal(Long userId, String authType, String phone, String name,
                         Collection<? extends GrantedAuthority> authorities) {
        this(userId, authType, phone, name);
        this.authorities = authorities != null ? authorities : Collections.emptyList();
    }

    // ================= UserDetails 接口实现 =================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /** JWT 认证不使用密码 */
    @Override
    public String getPassword() {
        return null;
    }

    /** 以 userId 作为 Spring Security 的用户名 */
    @Override
    public String getUsername() {
        return Objects.isNull(userId) ? null : String.valueOf(userId);
    }

}
