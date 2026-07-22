package cn.qdm.tob.modules.system.auth.api.mall;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.modules.system.auth.vo.TokenResponseVO;
import cn.qdm.tob.modules.system.auth.service.TokenProvider;
import cn.qdm.tob.modules.system.auth.token.WechatAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序商城端 — 微信认证 API
 * <p>
 * 微信登录走 {@code AuthenticationManager → WechatAuthenticationProvider}，
 * 由 Provider 调用 WxMaService 换 openId、查询/创建用户、签发 token。
 * Controller 仅做编排，不承载业务逻辑（遵循 RULES.md 模块分层）。
 */
@Slf4j
@RestController
@RequestMapping("/api/mall/auth")
public class WechatAuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @Value("${app.auth.jwt.expiration:3600}")
    private long jwtExpiration;

    public WechatAuthController(AuthenticationManager authenticationManager,
                                TokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * 微信一键登录（小程序商城端）
     *
     * @param phoneCode getPhoneNumber 返回的动态令牌，用于解密手机号
     * @param wxCode    wx.login() 返回的临时 code，用于 code2Session 换取 openId
     */
    @GetMapping("/wechat/login")
    public ResponseResult<TokenResponseVO> wechatLogin(
            @RequestParam("phoneCode") String phoneCode,
            @RequestParam("wxCode") String wxCode) {
        Authentication auth = authenticationManager.authenticate(
                new WechatAuthenticationToken(phoneCode, wxCode));
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
