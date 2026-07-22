package cn.qdm.tob.modules.product.catalog.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.product.catalog.domain.PrdCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类 Mapper
 */
@Mapper
public interface CategoryMapper extends TobBaseMapper<PrdCategory> {
}
