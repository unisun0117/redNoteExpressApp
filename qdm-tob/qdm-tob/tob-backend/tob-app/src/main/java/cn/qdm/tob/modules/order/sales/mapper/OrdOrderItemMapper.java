package cn.qdm.tob.modules.order.sales.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.order.sales.domain.OrdOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单商品明细 Mapper
 */
@Mapper
public interface OrdOrderItemMapper extends TobBaseMapper<OrdOrderItem> {
}
