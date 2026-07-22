package cn.qdm.tob.modules.system.sequence;

import cn.qdm.tob.infrastructure.util.BillNoGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("单据号生成器（Redis 按天自增）")
class BillNoGeneratorTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private BillNoGenerator billNoGenerator;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @BeforeEach
    void setUp() {
        billNoGenerator = new BillNoGenerator(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("nextOrderNo → 首次调用应返回前缀 + 日期 + 0001")
    void shouldGenerateFirstOrderNo() {
        String today = today();
        String key = "bill:daily:ORDER_NO:" + today;
        when(valueOperations.increment(key)).thenReturn(1L);

        String result = billNoGenerator.nextOrderNo();

        assertThat(result).isEqualTo("ORD" + today + "0001");
        verify(valueOperations).increment(key);
        verify(redisTemplate).expire(eq(key), eq(Duration.ofDays(2)));
    }

    @Test
    @DisplayName("nextOrderNo → 递增调用应正确格式化序号")
    void shouldIncrementOrderNo() {
        String today = today();
        String key = "bill:daily:ORDER_NO:" + today;
        when(valueOperations.increment(key)).thenReturn(123L);

        String result = billNoGenerator.nextOrderNo();

        assertThat(result).isEqualTo("ORD" + today + "0123");
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("nextTransactionNo → 首次调用应返回前缀 + 日期 + 6 位序号")
    void shouldGenerateFirstTransactionNo() {
        String today = today();
        String key = "bill:daily:ORD_FIN_TRANSACTION:" + today;
        when(valueOperations.increment(key)).thenReturn(1L);

        String result = billNoGenerator.nextTransactionNo();

        assertThat(result).isEqualTo("FIN" + today + "000001");
        verify(valueOperations).increment(key);
        verify(redisTemplate).expire(eq(key), eq(Duration.ofDays(2)));
    }

    @Test
    @DisplayName("nextTransactionNo → 序号超过 6 位应原样输出")
    void shouldNotTruncateLargeTransactionNo() {
        String today = today();
        String key = "bill:daily:ORD_FIN_TRANSACTION:" + today;
        when(valueOperations.increment(key)).thenReturn(999999L);

        String result = billNoGenerator.nextTransactionNo();

        assertThat(result).isEqualTo("FIN" + today + "999999");
    }

    @Test
    @DisplayName("nextOrderNo → Redis 跨天自动切新 Key，序号独立")
    void shouldAutoResetDailyByRedisKey() {
        String today = today();
        String keyToday = "bill:daily:ORDER_NO:" + today;
        when(valueOperations.increment(keyToday)).thenReturn(5L);

        String result = billNoGenerator.nextOrderNo();

        // 验证 Key 包含今天的日期（跨天自动切换 Key，INCR 从 0 开始）
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).increment(keyCaptor.capture());
        assertThat(keyCaptor.getValue()).contains(today);
        assertThat(result).contains(today);
    }

    @Test
    @DisplayName("nextOrderNo → Redis 返回 null 应抛异常")
    void shouldThrowWhenRedisReturnsNull() {
        String today = today();
        String key = "bill:daily:ORDER_NO:" + today;
        when(valueOperations.increment(key)).thenReturn(null);

        assertThatThrownBy(() -> billNoGenerator.nextOrderNo())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Redis INCR");
    }

    @Test
    @DisplayName("nextTransactionNo → 与 nextOrderNo 使用不同的 Key 命名空间")
    void shouldUseDifferentKeyNamespaces() {
        String today = today();
        String orderKey = "bill:daily:ORDER_NO:" + today;
        String transKey = "bill:daily:ORD_FIN_TRANSACTION:" + today;
        when(valueOperations.increment(orderKey)).thenReturn(1L);
        when(valueOperations.increment(transKey)).thenReturn(1L);

        billNoGenerator.nextOrderNo();
        billNoGenerator.nextTransactionNo();

        verify(valueOperations).increment(orderKey);
        verify(valueOperations).increment(transKey);
    }

    /** 今天的日期字符串 */
    private static String today() {
        return LocalDate.now().format(DATE_FMT);
    }
}
