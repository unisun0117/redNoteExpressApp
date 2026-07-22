package cn.qdm.tob.modules.system.auth.api.mall;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.modules.system.auth.service.RegistrationService;
import cn.qdm.tob.modules.system.auth.vo.RegisterCreationVO;
import cn.qdm.tob.modules.system.auth.vo.SmsVerifyVO;
import cn.qdm.tob.modules.system.auth.vo.TokenResponseVO;
import cn.qdm.tob.modules.system.auth.service.SmsCodeService;
import cn.qdm.tob.modules.system.auth.service.TokenProvider;
import cn.qdm.tob.modules.system.auth.token.SmsAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序商城端 — 通用认证 API（SMS 登录 / 注册 / Token 刷新 / 登出）
 * <p>
 * Controller 仅做编排，业务逻辑下沉到 service 层（遵循 RULES.md 模块分层）：
 * 短信验证码 → {@link SmsCodeService}，注册 → {@link RegistrationService}，token → {@link TokenProvider}。
 * 所在子目录 api/mall 与请求路径前缀 /api/mall 一致（遵循 RULES.md API 子目录约束）。
 */
@Slf4j
@RestController
@RequestMapping("/api/mall/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final SmsCodeService smsCodeService;
    private final TokenProvider tokenProvider;
    private final RegistrationService registrationService;

    @Value("${app.auth.jwt.expiration:3600}")
    private long jwtExpiration;

    public AuthController(AuthenticationManager authenticationManager,
                          SmsCodeService smsCodeService,
                          TokenProvider tokenProvider,
                          RegistrationService registrationService) {
        this.authenticationManager = authenticationManager;
        this.smsCodeService = smsCodeService;
        this.tokenProvider = tokenProvider;
        this.registrationService = registrationService;
    }

    /**
     * 请求短信验证码
     */
    @PostMapping("/sms/request")
    public ResponseResult<?> requestSmsCode(@RequestParam String mobile) {
        log.info("Request SMS code for phone: {}", mobile);
        smsCodeService.sendSmsCode(mobile);
        return ResponseResult.success();
    }

    /**
     * 短信验证并登录
     */
    @PostMapping("/sms/verify")
    public ResponseResult<TokenResponseVO> verifySmsCode(@RequestBody SmsVerifyVO request) {
        Authentication auth = authenticationManager.authenticate(new SmsAuthenticationToken(request.getPhone(), request.getSmsCode()));
        return ResponseResult.success(buildToken(auth));
    }

    /**
     * 小程序注册（姓名 + 手机号 + 短信验证码，可选微信 openId）
     * <p>
     * 业务逻辑见 {@link RegistrationService#register}。
     */
    @PostMapping("/register")
    public ResponseResult<TokenResponseVO> register(@RequestBody RegisterCreationVO request) {
        return ResponseResult.success(registrationService.register(request));
    }

    /**
     * 刷新 Token
     */
    @GetMapping("/token/refresh")
    public ResponseResult<TokenResponseVO> refreshToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        String token = authorization.replace("Bearer ", "");
        String newToken = tokenProvider.refreshToken(token);
        return ResponseResult.success(new TokenResponseVO(newToken, jwtExpiration));
    }

    /**
     * 登出（Token 加黑名单）
     */
    @PostMapping("/logout")
    public ResponseResult<?> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        String token = authorization.replace("Bearer ", "");
        try {
            String jti = tokenProvider.extractJti(token);
            // TODO: 将 jti 加入 Redis 黑名单
            log.info("Token {} added to blacklist", jti);
        } catch (Exception e) {
            log.warn("Failed to add token to blacklist: {}", e.getMessage());
        }
        return ResponseResult.success();
    }

    /**
     * 认证成功后，从 Authentication 提取 UserPrincipal 并生成 JWT
     */
    private TokenResponseVO buildToken(Authentication auth) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String token = tokenProvider.generateToken(principal);
        return new TokenResponseVO(token, jwtExpiration);
    }
}
