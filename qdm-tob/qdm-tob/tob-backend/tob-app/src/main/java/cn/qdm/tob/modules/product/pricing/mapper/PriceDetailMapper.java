package cn.qdm.tob.modules.product.pricing.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.product.pricing.domain.PrdPriceDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 价格组明细 Mapper
 */
@Mapper
public interface PriceDetailMapper extends TobBaseMapper<PrdPriceDetail> {
}
