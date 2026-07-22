package cn.qdm.tob.modules.system.sequence.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.system.sequence.domain.SysSequence;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;

/**
 * 通用序列号 Mapper。
 * <p>原子自增通过 UPDATE 语句利用 MySQL 行锁保证并发安全，无需额外加锁。</p>
 */
@Mapper
public interface SysSequenceMapper extends TobBaseMapper<SysSequence> {

    /**
     * 按 seq_key 查询序列配置。
     *
     * @param seqKey 序列唯一标识
     * @return 序列实体，不存在返回 Optional.empty()
     */
    default Optional<SysSequence> findBySeqKey(String seqKey) {
        return lambdaSelectOne(w -> w.eq(SysSequence::getSeqKey, seqKey));
    }

    /**
     * 原子自增 current_val（current_val = current_val + step），利用 DB 行锁保证并发安全。
     * <p>注意：该方法只执行 UPDATE，调用方需自行 SELECT 获取新值。</p>
     *
     * @param seqKey 序列唯一标识
     * @return 受影响行数（1 = 成功，0 = key 不存在）
     */
    @Update("UPDATE sys_sequence SET current_val = current_val + step WHERE seq_key = #{seqKey}")
    int incrementAndGet(@Param("seqKey") String seqKey);
}
