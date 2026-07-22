package cn.qdm.tob.modules.system.sequence.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.system.sequence.api.internal.SequenceApi;
import cn.qdm.tob.modules.system.sequence.api.internal.dto.SequenceResult;
import cn.qdm.tob.modules.system.sequence.domain.SysSequence;
import cn.qdm.tob.modules.system.sequence.mapper.SysSequenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通用序列号服务实现。
 * <p>通过 DB 行锁（UPDATE WHERE seq_key = ?）保证并发安全，每次调用原子自增并返回格式化后的值。</p>
 * <p>实现 {@link SequenceApi}，供其他模块通过接口注入使用。</p>
 */
@Service
@RequiredArgsConstructor
public class SequenceServiceImpl extends TobBaseService<SysSequenceMapper, SysSequence> implements SequenceApi {

    /**
     * {@inheritDoc}
     * <p>步骤：校验 seqKey 存在 → 原子自增 → 查询新值 → 按 formatter 格式化返回。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SequenceResult next(String seqKey) {
        SysSequence seq = baseMapper.findBySeqKey(seqKey).orElse(null);
        AssertUtils.notNull(seq, "序列不存在: " + seqKey);

        baseMapper.incrementAndGet(seqKey);
        SysSequence updated = baseMapper.findBySeqKey(seqKey).orElseThrow();
        long raw = updated.getCurrentVal();

        String formatted;
        if (seq.getFormatter() != null && !seq.getFormatter().isEmpty()) {
            formatted = String.format(seq.getFormatter(), raw).trim();
        } else {
            formatted = String.valueOf(raw);
        }

        return new SequenceResult(seqKey, formatted, raw);
    }
}
