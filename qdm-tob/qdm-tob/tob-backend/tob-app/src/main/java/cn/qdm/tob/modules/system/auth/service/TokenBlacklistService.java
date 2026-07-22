package cn.qdm.tob.modules.system.auth.service;

import cn.qdm.tob.infrastructure.autoconfigure.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * JWT Token 黑名单服务。
 * <p>退出登录时将 JWT 的 jti 加入 Redis 黑名单，Toke 在有效期内无法复用。</p>
 * <p>Key 格式：token:blacklist:{jti}，TTL 与 JWT 过期时间一致。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    /** 将 token 的 jti 加入黑名单 */
    public void blacklist(String jti) {
        String key = BLACKLIST_KEY_PREFIX + jti;
        Duration ttl = Duration.ofSeconds(jwtProperties.getExpiration());
        redisTemplate.opsForValue().set(key, "1", ttl);
        log.info("Token 已加入黑名单: jti={}, ttl={}s", jti, ttl.getSeconds());
    }

    /** 检查 jti 是否在黑名单中 */
    public boolean isBlacklisted(String jti) {
        if (jti == null) return false;
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + jti));
    }
}
