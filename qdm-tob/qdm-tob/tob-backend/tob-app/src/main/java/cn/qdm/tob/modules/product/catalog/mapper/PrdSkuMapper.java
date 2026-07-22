package cn.qdm.tob.modules.product.catalog.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.product.catalog.domain.PrdSku;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品条码 Mapper
 */
@Mapper
public interface PrdSkuMapper extends TobBaseMapper<PrdSku> {
}
