package cn.qdm.tob.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import cn.qdm.tob.modules.system.common.CacheKeys;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 配置
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfiguration {

    /** 统一缓存 TTL：1 天（变更时主动清，TTL 仅兜底） */
    private static final Duration DEFAULT_TTL = Duration.ofDays(1);
    /** 字典项缓存 TTL：10 分钟 */
    private static final Duration DICT_ITEMS_TTL = Duration.ofMinutes(10);

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(CacheKeys.DICT_ITEMS, defaultConfig.entryTtl(DICT_ITEMS_TTL));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    /**
     * Redis 异常时吞掉，穿透查 DB 兜底。
     * 不丢给上层，避免缓存挂了拖垮业务。
     */
    @Bean
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.warn("Cache get 失败，降级查 DB: cache={}, key={}", cache.getName(), key, e);
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.warn("Cache put 失败: cache={}, key={}", cache.getName(), key, e);
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.warn("Cache evict 失败: cache={}, key={}", cache.getName(), key, e);
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.warn("Cache clear 失败: cache={}", cache.getName(), e);
            }
        };
    }

    /**
     * 统一 RedisTemplate：key 用 String，value 通过 RedisSerializer.json() 自动 JSON 序列化。
     * 供 TokenBlacklistService 等场景使用。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(RedisSerializer.json());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(RedisSerializer.json());
        template.afterPropertiesSet();
        return template;
    }
}
