package cn.qdm.tob.modules.system.auth.service;

import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.exception.TokenValidationException;
import cn.qdm.tob.infrastructure.autoconfigure.JwtProperties;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.modules.system.auth.enums.AuthType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * JWT Token 提供者
 * 基于 Spring Security NimbusJwtEncoder / NimbusJwtDecoder。
 */
@Slf4j
@Service
public class TokenProvider {

    private final NimbusJwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    private final long jwtExpirationSeconds;

    public TokenProvider(JwtProperties jwtProperties) {
        this.jwtExpirationSeconds = jwtProperties.getExpiration();
        SecretKey key = new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.encoder = NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build();
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /**
     * 生成 JWT Token
     * <p>订阅 sub 前缀按认证类型区分：Admin(CAS/WECOM) 用 "operator:"，Mall(SMS/WECHAT) 用 "user:"。</p>
     * 注意：不再写入 aut claim，权限实时从 Redis 查询
     */
    public String generateToken(UserPrincipal principal) {
        String jti = principal.getJti() != null ? principal.getJti() : UUID.randomUUID().toString();
        Instant now = Instant.now();

        String subject = AuthType.isAdmin(principal.getAuthType())
                ? "operator:" + principal.getUserId()
                : "user:" + principal.getUserId();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .claim("uid", principal.getUserId())
                .claim("tp", principal.getAuthType())
                .claim("mbl", principal.getPhone() != null ? principal.getPhone() : "")
                .claim("nm", principal.getName() != null ? principal.getName() : "")
                .claim("ec", principal.getEmployeeCode() != null ? principal.getEmployeeCode() : "")
                .id(jti)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtExpirationSeconds))
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * 验证并解析 Token
     * 注意：不再从 aut claim 读取权限，权限由 JwtAuthenticationFilter 从 Redis 加载
     */
    public UserPrincipal validateToken(String token) throws TokenValidationException {
        try {
            Jwt jwt = decoder.decode(token);

            UserPrincipal principal = new UserPrincipal(
                    jwt.getClaim("uid"),
                    jwt.getClaim("tp"),
                    jwt.getClaim("mbl"),
                    jwt.getClaim("nm"),
                    Collections.emptyList());
            principal.setJti(jwt.getId());
            principal.setEmployeeCode(jwt.getClaim("ec"));
            return principal;
        } catch (JwtException e) {
            log.error("Token validation failed: ", e);
            throw new TokenValidationException(ErrorCode.UNAUTHORIZED.code(), "Token 验证失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Token validation error: ", e);
            throw new TokenValidationException(ErrorCode.UNAUTHORIZED.code(), "Token 验证异常");
        }
    }

    /**
     * 刷新 Token
     */
    public String refreshToken(String token) throws TokenValidationException {
        UserPrincipal principal = validateToken(token);
        principal.setJti(UUID.randomUUID().toString());
        return generateToken(principal);
    }

    /**
     * 从 Token 中提取 JTI（用于黑名单检查）
     */
    public String extractJti(String token) throws TokenValidationException {
        return validateToken(token).getJti();
    }
}
