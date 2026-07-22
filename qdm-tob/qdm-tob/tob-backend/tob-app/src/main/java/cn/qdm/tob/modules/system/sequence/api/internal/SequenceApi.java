package cn.qdm.tob.modules.system.sequence.api.internal;

import cn.qdm.tob.modules.system.sequence.api.internal.dto.SequenceResult;

/**
 * 通用序列号生成 API（供其他模块注入使用）。
 * <p>
 * 每种序列在 sys_sequence 表中以 seq_key 标识，调用 {@link #next(String)} 后自动原子自增。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Autowired
 * private SequenceApi sequenceApi;
 *
 * SequenceResult result = sequenceApi.next("ORDER_NO");
 * // result.getFormattedValue() → "202607080001"
 * // result.getRawValue()      → 1
 * }</pre>
 */
public interface SequenceApi {

    /**
     * 获取指定序列的下一个值。
     * <p>该方法在事务内执行 UPDATE + SELECT，利用 DB 行锁保证并发安全。</p>
     *
     * @param seqKey 序列标识（sys_sequence.seq_key），如 "USER_RECOMMEND_CODE"
     * @return 格式化后的值和原始值，序列不存在时抛异常
     */
    SequenceResult next(String seqKey);
}
