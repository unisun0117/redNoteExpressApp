package cn.qdm.tob.modules.product.pricing.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.product.pricing.domain.PrdPriceGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * 价格组 Mapper
 */
@Mapper
public interface PriceGroupMapper extends TobBaseMapper<PrdPriceGroup> {
}
