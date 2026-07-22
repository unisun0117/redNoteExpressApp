package cn.qdm.tob.modules.system.auth.provider;

import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.modules.system.auth.enums.AuthType;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.modules.system.auth.service.SmsCodeService;
import cn.qdm.tob.modules.system.auth.token.SmsAuthenticationToken;
import cn.qdm.tob.modules.system.user.domain.SysUser;
import cn.qdm.tob.modules.system.user.enums.UserStatus;
import cn.qdm.tob.modules.system.user.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 小程序短信认证 Provider
 * 验证短信码 → 查询/创建 User → 返回已认证的 SmsAuthenticationToken（User 无权限）
 */
@Slf4j
@Component
public class SmsAuthenticationProvider implements AuthenticationProvider {

    private final SmsCodeService smsCodeService;
    private final SysUserMapper userMapper;

    public SmsAuthenticationProvider(SmsCodeService smsCodeService, SysUserMapper userMapper) {
        this.smsCodeService = smsCodeService;
        this.userMapper = userMapper;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken token = (SmsAuthenticationToken) authentication;
        String phone = (String) token.getPrincipal();
        String smsCode = (String) token.getCredentials();

        // 1. 验证短信码（失败抛 InvalidSmsCodeException）
        smsCodeService.verifySmsCode(phone, smsCode);

        // 2. 查询或创建 User
        SysUser user = userMapper.findByMobile(phone).orElseGet(() -> {
            SysUser newUser = new SysUser();
            newUser.setMobile(phone);
            newUser.setStatus(UserStatus.ACTIVE);
            newUser.setRegisteredAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(newUser);
            log.info("New user created for phone: {}", phone);
            return newUser;
        });

        // 3. 校验账号状态（禁用则拒绝登录）
        if (UserStatus.INACTIVE.equals(user.getStatus()) || UserStatus.FROZEN.equals(user.getStatus())) {
            throw new TobServiceException(ErrorCode.FORBIDDEN.code(), "账号已被禁用，请联系管理员");
        }

        // 4. 更新最近登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 5. 构造 UserPrincipal（优先 real_name，微信注册账号 real_name 为空则回退 wechat_nickname）
        String displayName = user.getRealName() != null ? user.getRealName() : user.getWechatNickname();
        UserPrincipal principal = new UserPrincipal(
                user.getId(), AuthType.SMS.toString(), user.getMobile(), displayName);

        return new SmsAuthenticationToken(principal, Collections.emptyList());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
