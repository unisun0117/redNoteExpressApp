package cn.qdm.tob.modules.product.catalog.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.product.catalog.domain.PrdProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品资料主表 Mapper
 */
@Mapper
public interface PrdProductMapper extends TobBaseMapper<PrdProduct> {
}
