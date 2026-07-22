package cn.qdm.tob.modules.product.catalog;

import cn.qdm.tob.modules.product.catalog.domain.PrdProduct;
import cn.qdm.tob.modules.product.catalog.vo.ProductViewVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 商品资料模块 MapStruct 对象映射器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapping {

    /** 实体 → ViewVO */
    ProductViewVO toView(PrdProduct entity);

    /** 批量实体 → ViewVO */
    List<ProductViewVO> toViewList(List<PrdProduct> entities);
}
