package cn.qdm.tob.modules.product.catalog;

import cn.qdm.tob.modules.product.catalog.domain.PrdCategory;
import cn.qdm.tob.modules.product.catalog.vo.CategoryViewVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 商品分类模块 MapStruct 对象映射器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapping {

    /** 实体 → ViewVO */
    CategoryViewVO toView(PrdCategory entity);

    /** 批量实体 → ViewVO */
    List<CategoryViewVO> toViewList(List<PrdCategory> entities);
}
