package cn.qdm.tob.infrastructure.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 单据号生成器，基于 Redis 原子自增实现按天重置。
 *
 * <p>每种单据类型一个专用方法，格式参数硬编码在方法内，零配置、零 DB 依赖。</p>
 *
 * <h3>设计要点</h3>
 * <ul>
 *     <li><b>Redis Key</b>：{@code bill:daily:{type}:{yyyyMMdd}}，跨天自动切新 Key，天然按天归零</li>
 *     <li><b>原子性</b>：使用 {@code INCR} 保证并发安全，无需加锁</li>
 *     <li><b>自动清理</b>：每次新 Key 创建时设置 2 天 TTL，过期 Key 自动删除</li>
 *     <li><b>零配置</b>：前缀、日期格式、数字位数均硬编码在方法签名中，编译期保证正确</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Autowired
 * private BillNoGenerator billNoGenerator;
 *
 * String orderNo = billNoGenerator.nextOrderNo();       // → ORD202607130001
 * String transNo = billNoGenerator.nextTransactionNo(); // → FIN20260713000001
 * }</pre>
 */
@Component
@RequiredArgsConstructor
public class BillNoGenerator {

    private final StringRedisTemplate redisTemplate;

    /** Redis Key 前缀 */
    private static final String KEY_PREFIX = "bill:daily:";

    /** 历史 Key 保留时长（2 天），便于跨天追溯昨天的单据号 */
    private static final Duration KEY_TTL = Duration.ofDays(2);

    /** 日期格式：yyyyMMdd */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 生成下一个订单号。
     * <p>格式：ORD + yyyyMMdd + 4 位序号</p>
     * <p>示例：ORD202607130001</p>
     *
     * @return 格式化后的订单号
     */
    public String nextOrderNo() {
        return next("ORDER_NO", "ORD", "%04d");
    }

    /**
     * 生成下一个财务流水号。
     * <p>格式：FIN + yyyyMMdd + 6 位序号</p>
     * <p>示例：FIN20260713000001</p>
     *
     * @return 格式化后的财务流水号
     */
    public String nextTransactionNo() {
        return next("ORD_FIN_TRANSACTION", "FIN", "%06d");
    }

    // ==================== 私有方法 ====================

    /**
     * 通用序列号生成。
     *
     * @param type      业务类型标识（用于 Redis Key 区分）
     * @param prefix    固定前缀，如 ORD、FIN
     * @param numFormat 数字部分格式化模板，如 %04d（4 位零填充）
     * @return 格式化后的完整单号
     */
    private String next(String type, String prefix, String numFormat) {
        String date = LocalDate.now().format(DATE_FMT);
        String key = KEY_PREFIX + type + ":" + date;

        Long seq = redisTemplate.opsForValue().increment(key);
        if (seq == null) {
            throw new IllegalStateException("Redis INCR 返回 null，序列号生成失败: " + key);
        }

        // 首次创建时设置过期，自动清理历史 Key
        if (seq <= 3L) {
            redisTemplate.expire(key, KEY_TTL);
        }

        return prefix + date + String.format(numFormat, seq);
    }
}
