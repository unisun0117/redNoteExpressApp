package cn.qdm.tob.modules.system.sequence.api.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 序列号生成结果，由 {@link cn.qdm.tob.modules.system.sequence.api.internal.SequenceApi#next(String)} 返回。
 */
@Data
@AllArgsConstructor
public class SequenceResult {
    /** 序列标识，对应 sys_sequence.seq_key */
    private String seqKey;
    /** 格式化后的序列值，如 "0001"、"202607080001"（取决于 formatter 模板） */
    private String formattedValue;
    /** 原始自增数值（未格式化），如 1、100 */
    private Long rawValue;
}
