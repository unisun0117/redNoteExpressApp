package cn.qdm.tob.modules.order.sales.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.order.sales.domain.OrdCartItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车 Mapper
 */
@Mapper
public interface OrdCartItemMapper extends TobBaseMapper<OrdCartItem> {
}
