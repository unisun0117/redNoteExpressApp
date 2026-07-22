package cn.qdm.tob.modules.product.pricing;

import cn.qdm.tob.modules.product.pricing.domain.PrdPriceDetail;
import cn.qdm.tob.modules.product.pricing.vo.PriceDetailCreationVO;
import cn.qdm.tob.modules.product.pricing.vo.PriceDetailExportVO;
import cn.qdm.tob.modules.product.pricing.vo.PriceDetailViewVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 价格组明细 MapStruct 对象映射器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PriceDetailMapping {

    /** CreationVO → 实体 */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "changeReason", ignore = true)
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "pendingPrice", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", ignore = true)
    PrdPriceDetail toEntity(PriceDetailCreationVO vo);

    /** 实体 → ViewVO（含审批状态描述，priceGroupName 由 Service 层联查填充） */
    @Mapping(target = "approvalStatus", expression = "java(entity.getApprovalStatus() != null ? entity.getApprovalStatus().name() : null)")
    @Mapping(target = "approvalStatusDescription", expression = "java(entity.getApprovalStatus() != null ? entity.getApprovalStatus().getDescription() : null)")
    @Mapping(target = "priceGroupName", ignore = true)
    PriceDetailViewVO toView(PrdPriceDetail entity);

    /** 批量实体 → ViewVO */
    List<PriceDetailViewVO> toViewList(List<PrdPriceDetail> entities);

    /** 实体 → ExportVO（priceGroupName 由 Service 层联查填充） */
    @Mapping(target = "approvalStatus", expression = "java(entity.getApprovalStatus() != null ? entity.getApprovalStatus().getDescription() : null)")
    @Mapping(target = "priceGroupName", ignore = true)
    PriceDetailExportVO toExport(PrdPriceDetail entity);

    /** 批量实体 → ExportVO */
    List<PriceDetailExportVO> toExportList(List<PrdPriceDetail> entities);
}
