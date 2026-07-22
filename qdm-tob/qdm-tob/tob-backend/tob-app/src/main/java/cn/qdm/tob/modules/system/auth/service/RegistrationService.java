package cn.qdm.tob.modules.system.auth.service;

import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.modules.system.auth.RegisterMapping;
import cn.qdm.tob.modules.system.auth.enums.AuthType;
import cn.qdm.tob.modules.system.auth.vo.RegisterCreationVO;
import cn.qdm.tob.modules.system.auth.vo.TokenResponseVO;
import cn.qdm.tob.modules.system.user.domain.SysUser;
import cn.qdm.tob.modules.system.user.enums.UserStatus;
import cn.qdm.tob.modules.system.user.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 小程序客户注册服务（业务逻辑层，遵循 RULES.md 模块分层——Controller 仅编排）
 * <p>
 * 流程：校验入参 → 校验短信验证码 → 手机号查重 → 创建账号（落 realName）→ 签发 JWT。
 * - 实体转换经 {@link RegisterMapping}（MapStruct），不手写；
 * - 用户数据访问经 {@link SysUserService}（Modulith：不直访 user 子域 Mapper）。
 */
@Slf4j
@Service
public class RegistrationService {

    private final SmsCodeService smsCodeService;
    private final TokenProvider tokenProvider;
    private final SysUserService userService;
    private final RegisterMapping registerMapping;

    @Value("${app.auth.jwt.expiration:3600}")
    private long jwtExpiration;

    /**
     * 临时跳过短信验证码开关（与 SmsCodeService 同源配置）。
     * 开启时注册接口不再强制要求 smsCode 非空，verifySmsCode 也会被短路放行。
     */
    @Value("${app.auth.sms.bypass-enabled:false}")
    private boolean smsBypassEnabled;

    public RegistrationService(SmsCodeService smsCodeService,
                               TokenProvider tokenProvider,
                               SysUserService userService,
                               RegisterMapping registerMapping) {
        this.smsCodeService = smsCodeService;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.registerMapping = registerMapping;
    }

    /**
     * 注册并签发 token（注册成功即登录）
     */
    public TokenResponseVO register(RegisterCreationVO request) {
        String phone = request.getPhone();
        String realName = request.getRealName();
        String smsCode = request.getSmsCode();

        // 1. 基础校验
        if (realName == null || realName.isBlank()) {
            throw new TobServiceException(ErrorCode.BAD_REQUEST.code(), "请输入姓名");
        }
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new TobServiceException(ErrorCode.BAD_REQUEST.code(), "请输入正确的手机号");
        }
        // 短信验证码必填校验（bypass 模式下放行，便于本地联调）
        if (!smsBypassEnabled && (smsCode == null || smsCode.isBlank())) {
            throw new TobServiceException(ErrorCode.BAD_REQUEST.code(), "请输入验证码");
        }

        // 2. 验证码校验（失败抛 InvalidSmsCodeException / 423）
        smsCodeService.verifySmsCode(phone, smsCode);

        // 3. 手机号查重
        if (userService.findByMobile(phone).isPresent()) {
            throw new TobServiceException(409, "该手机号已注册，请直接登录");
        }

        // 4. 创建账号（VO→Entity 走 MapStruct，系统字段在此设置）
        SysUser newUser = registerMapping.toEntity(request);
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setRegisteredAt(LocalDateTime.now());
        newUser.setLastLoginAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        userService.insert(newUser);
        log.info("New customer registered: phone={}, realName={}, id={}", phone, realName, newUser.getId());

        // 5. 签发 JWT（携带姓名，首页可直接展示「欢迎，{姓名}」）
        UserPrincipal principal = new UserPrincipal(
                newUser.getId(), AuthType.SMS.toString(), phone, realName);
        String token = tokenProvider.generateToken(principal);
        return new TokenResponseVO(token, jwtExpiration);
    }
}
