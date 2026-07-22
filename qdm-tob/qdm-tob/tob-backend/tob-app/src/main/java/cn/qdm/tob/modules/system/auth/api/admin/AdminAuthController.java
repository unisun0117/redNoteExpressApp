package cn.qdm.tob.modules.system.auth.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.infrastructure.security.annotation.CurrentUser;
import cn.qdm.tob.modules.system.auth.vo.TokenResponseVO;
import cn.qdm.tob.modules.system.auth.service.TokenBlacklistService;
import cn.qdm.tob.modules.system.auth.service.TokenProvider;
import cn.qdm.tob.modules.system.auth.token.CasAuthenticationToken;
import cn.qdm.tob.modules.system.auth.token.WeComAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Web 和企业微信管理端 — 认证 API（CAS / 企业微信登录）
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${app.auth.jwt.expiration:3600}")
    private long jwtExpiration;

    public AdminAuthController(AuthenticationManager authenticationManager,
                               TokenProvider tokenProvider,
                               TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * CAS 登录（Web 管理端）
     */
    @PostMapping("/cas/login")
    public ResponseResult<TokenResponseVO> casLogin(@RequestParam String ticket,
                                                    @RequestParam String service) {
        Authentication auth = authenticationManager.authenticate(
                new CasAuthenticationToken(ticket, service));
        return ResponseResult.success(buildToken(auth));
    }

    /**
     * 登出：将当前 Token 的 jti 加入 Redis 黑名单，使 Token 在有效期内无法复用。
     */
    @PostMapping("/logout")
    public ResponseResult<?> logout(@CurrentUser UserPrincipal user) {
        log.info("用户登出: userId={}, name={}, jti={}", user.getUserId(), user.getName(), user.getJti());
        tokenBlacklistService.blacklist(user.getJti());
        return ResponseResult.success();
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current-user")
    public ResponseResult<UserPrincipal> currentUser(@CurrentUser UserPrincipal user) {
        return ResponseResult.success(user);
    }

    /**
     * 企业微信登录（管理端）
     *
     * @deprecated 企微登录已统一走 CAS 认证流程（WeCom H5 → CAS → ticket → {@link #casLogin}）。
     * 此接口保留供未来扩展使用，当前不应被调用。
     */
    @Deprecated
    @GetMapping("/wecom/login")
    public ResponseResult<TokenResponseVO> wecomLogin(@RequestParam String code) {
        Authentication auth = authenticationManager.authenticate(new WeComAuthenticationToken(code));
        return ResponseResult.success(buildToken(auth));
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
