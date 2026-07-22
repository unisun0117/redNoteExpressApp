package cn.qdm.tob.modules.customer.operation.salesregion;

import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.dto.SalesRegionDetailDTO;
import cn.qdm.tob.modules.customer.operation.salesregion.domain.OprSalesRegion;
import cn.qdm.tob.modules.customer.operation.salesregion.vo.SalesRegionCreationVO;
import cn.qdm.tob.modules.customer.operation.salesregion.vo.SalesRegionEditVO;
import cn.qdm.tob.modules.customer.operation.salesregion.vo.SalesRegionViewVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 销售大区模块 MapStruct 对象映射器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SalesRegionMapping {

    /** CreationVO → 实体 */
    OprSalesRegion toEntity(SalesRegionCreationVO vo);

    /** EditVO → 实体 */
    OprSalesRegion toEntity(SalesRegionEditVO vo);

    /** 实体 → ViewVO */
    SalesRegionViewVO toView(OprSalesRegion entity);

    /** 批量实体 → ViewVO */
    List<SalesRegionViewVO> toViewList(List<OprSalesRegion> entities);

    SalesRegionDetailDTO toDto(OprSalesRegion entity);
}
