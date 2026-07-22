package cn.qdm.tob.modules.product.pricing;

import cn.qdm.tob.modules.product.pricing.domain.PrdPriceGroup;
import cn.qdm.tob.modules.product.pricing.vo.PriceGroupCreationVO;
import cn.qdm.tob.modules.product.pricing.vo.PriceGroupViewVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 价格组 MapStruct 对象映射器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PriceGroupMapping {

    /** CreationVO → 实体 */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", ignore = true)
    PrdPriceGroup toEntity(PriceGroupCreationVO vo);

    /** 实体 → ViewVO */
    PriceGroupViewVO toView(PrdPriceGroup entity);

    /** 批量实体 → ViewVO */
    List<PriceGroupViewVO> toViewList(List<PrdPriceGroup> entities);
}
