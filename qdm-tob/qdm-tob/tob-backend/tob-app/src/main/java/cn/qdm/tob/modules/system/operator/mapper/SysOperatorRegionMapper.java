package cn.qdm.tob.modules.system.operator.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.system.operator.domain.SysOperatorRegion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 运营人员-销售大区绑定 Mapper
 */
@Mapper
public interface SysOperatorRegionMapper extends TobBaseMapper<SysOperatorRegion> {

    default List<SysOperatorRegion> findByOperatorId(Long operatorId) {
        return lambdaSelect(w -> w.eq(SysOperatorRegion::getOperatorId, operatorId));
    }

    default int deleteByOperatorId(Long operatorId) {
        return lambdaDelete(w -> w.eq(SysOperatorRegion::getOperatorId, operatorId));
    }

    /** 批量插入 */
    void batchInsert(@Param("list") List<SysOperatorRegion> list);

    /** 精准删除指定大区绑定（增量覆盖用） */
    default void deleteByOperatorIdAndRegionCodes(Long operatorId, List<String> regionCodes) {
        if (regionCodes == null || regionCodes.isEmpty()) return;
        delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysOperatorRegion>()
                .eq(SysOperatorRegion::getOperatorId, operatorId)
                .in(SysOperatorRegion::getRegionCode, regionCodes));
    }
}
