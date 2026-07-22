package cn.qdm.tob.modules.order.sales.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.order.sales.domain.OrdOrderDiff;
import org.apache.ibatis.annotations.Mapper;

/**
 * 差补单 Mapper
 */
@Mapper
public interface OrdOrderDiffMapper extends TobBaseMapper<OrdOrderDiff> {
}
