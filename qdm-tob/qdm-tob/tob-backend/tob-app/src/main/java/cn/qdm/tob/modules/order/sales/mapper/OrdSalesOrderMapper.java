package cn.qdm.tob.modules.order.sales.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.order.sales.domain.OrdSalesOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售订单 Mapper
 */
@Mapper
public interface OrdSalesOrderMapper extends TobBaseMapper<OrdSalesOrder> {
}
