package cn.qdm.tob.modules.system.auth.provider;

import cn.qdm.tob.framework.exception.AuthenticationException;
import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.modules.system.operator.domain.SysOperator;
import cn.qdm.tob.modules.system.operator.enums.OperatorType;
import cn.qdm.tob.modules.system.operator.enums.OperatorStatus;
import cn.qdm.tob.modules.system.operator.mapper.SysOperatorMapper;
import cn.qdm.tob.modules.system.auth.enums.AuthType;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.modules.system.auth.token.WeComAuthenticationToken;
import cn.qdm.tob.modules.system.rbac.service.OperatorAuthorityLoader;
import cn.qdm.tob.client.wecom.WeComClient;
import cn.qdm.tob.client.wecom.dto.WeComUserInfoResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 企业微信认证 Provider
 *
 * @deprecated 企微登录已统一走 CAS 认证流程（WeCom H5 → CAS → ticket → CasAuthenticationProvider）。
 * 此 Provider 保留供未来扩展使用，当前不应被调用。
 *
 * @see CasAuthenticationProvider
 */
@Slf4j
@Component
@Deprecated
public class WeComAuthenticationProvider implements AuthenticationProvider {

    private final SysOperatorMapper adminMapper;
    private final OperatorAuthorityLoader authorityLoader;
    private final WeComClient weComClient;

    public WeComAuthenticationProvider(SysOperatorMapper adminMapper,
                                       OperatorAuthorityLoader authorityLoader,
                                       WeComClient weComClient) {
        this.adminMapper = adminMapper;
        this.authorityLoader = authorityLoader;
        this.weComClient = weComClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        WeComAuthenticationToken token = (WeComAuthenticationToken) authentication;
        String code = (String) token.getPrincipal();

        // code → userId
        WeComUserInfoResponseDTO userInfo = weComClient.getUserInfo(code);
        if (!userInfo.isSucceed()) {
            throw new AuthenticationException(ErrorCode.UNAUTHORIZED.code(), "企业微信获取用户信息失败: " + userInfo.getErrMsg());
        }
        String wecomUserId = userInfo.getUserId();
        if (wecomUserId == null || wecomUserId.isEmpty()) {
            throw new AuthenticationException(ErrorCode.UNAUTHORIZED.code(), "企业微信认证失败: userId 为空");
        }

        // 查询或创建 Admin
        SysOperator admin = adminMapper.findByLoginId(wecomUserId).orElseGet(() -> {
            SysOperator newAdmin = new SysOperator();
            newAdmin.setLoginId(wecomUserId);
            newAdmin.setMobile(wecomUserId);
            newAdmin.setStatus(OperatorStatus.ACTIVE);
            newAdmin.setType(OperatorType.SALESMAN);
            // 首次登录尚无 token，审计字段显式设置避免 handler 填"未知"
            newAdmin.setCreatedBy("企微自助创建");
            adminMapper.insert(newAdmin);
            log.info("New admin created for WeChat Work userId: {}", wecomUserId);
            return newAdmin;
        });

        // 加载权限
        Collection<GrantedAuthority> authorities = authorityLoader.loadAuthorities(admin.getId());

        // 构造已认证 Token
        UserPrincipal principal = new UserPrincipal(
                admin.getId(), AuthType.WECOM.toString(), admin.getEmail(), admin.getRealName(), authorities);

        return new WeComAuthenticationToken(principal, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WeComAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
