package cn.qdm.tob.modules.system.auth;

import cn.qdm.tob.modules.system.operator.mapper.SysOperatorMapper;
import cn.qdm.tob.modules.system.operator.service.OperatorStatusFilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("停用黑名单服务")
class OperatorStatusFilterTest {

    private static final String INACTIVE_SET_KEY = "operator:inactive:set";
    private static final Long ACTIVE_USER = 1L;
    private static final Long INACTIVE_USER = 2L;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SysOperatorMapper operatorMapper;

    @Mock
    private SetOperations<String, String> setOperations;

    private OperatorStatusFilterService service;

    @BeforeEach
    void setUp() {
        service = new OperatorStatusFilterService(redisTemplate, operatorMapper);
    }

    // ===== isInactive =====

    @Test
    @DisplayName("SISMEMBER 返回 true → 已停用")
    void shouldReturnTrueWhenSismemberTrue() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember(INACTIVE_SET_KEY, String.valueOf(INACTIVE_USER))).thenReturn(true);

        assertThat(service.isInactive(INACTIVE_USER)).isTrue();
    }

    @Test
    @DisplayName("SISMEMBER 返回 false → 未停用")
    void shouldReturnFalseWhenSismemberFalse() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember(INACTIVE_SET_KEY, String.valueOf(ACTIVE_USER))).thenReturn(false);

        assertThat(service.isInactive(ACTIVE_USER)).isFalse();
    }

    // ===== addToBlacklist =====

    @Test
    @DisplayName("停用 → SADD")
    void shouldSaddOnInactivate() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        service.addToBlacklist(ACTIVE_USER);

        verify(setOperations).add(INACTIVE_SET_KEY, String.valueOf(ACTIVE_USER));
    }

    // ===== removeFromBlacklist =====

    @Test
    @DisplayName("启用 → SREM")
    void shouldSremOnActivate() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        service.removeFromBlacklist(INACTIVE_USER);

        verify(setOperations).remove(INACTIVE_SET_KEY, String.valueOf(INACTIVE_USER));
    }

    // ===== initBlacklist =====

    @Test
    @DisplayName("启动初始化 → DB INACTIVE+LOCKED → SADD 全量")
    void shouldInitBlacklistFromDb() {
        List<Long> inactiveIds = List.of(2L, 5L, 8L);
        when(operatorMapper.findAllInactiveIds()).thenReturn(inactiveIds);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        service.initBlacklist();

        verify(operatorMapper).findAllInactiveIds();
        verify(setOperations).add(eq(INACTIVE_SET_KEY), eq("2"), eq("5"), eq("8"));
    }

    @Test
    @DisplayName("启动初始化 → DB 无停用 → 不调 SADD")
    void shouldNotSaddWhenNoInactiveUsers() {
        when(operatorMapper.findAllInactiveIds()).thenReturn(Collections.emptyList());

        service.initBlacklist();

        verify(operatorMapper).findAllInactiveIds();
        verify(redisTemplate, never()).opsForSet();
    }
}
