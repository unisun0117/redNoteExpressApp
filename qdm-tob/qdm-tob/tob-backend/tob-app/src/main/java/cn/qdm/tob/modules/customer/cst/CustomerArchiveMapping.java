package cn.qdm.tob.modules.customer.cst;

import cn.qdm.tob.infrastructure.security.resolver.CustomerContext;
import cn.qdm.tob.modules.customer.cst.domain.CstArchiveAuditLog;
import cn.qdm.tob.modules.customer.cst.domain.CstArchiveUserBinding;
import cn.qdm.tob.modules.customer.cst.domain.CstCompanyArchive;
import cn.qdm.tob.modules.customer.cst.vo.CustomerArchiveCreateVO;
import cn.qdm.tob.modules.customer.cst.vo.CustomerArchiveSummaryVO;
import cn.qdm.tob.modules.customer.cst.vo.CustomerArchiveViewVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 客户档案模块 MapStruct 对象映射器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerArchiveMapping {

    /** CreateVO → 实体 */
    CstCompanyArchive toEntity(CustomerArchiveCreateVO vo);

    /** 实体 → SummaryVO */
    CustomerArchiveSummaryVO toSummary(CstCompanyArchive entity);

    /** 实体列表 → SummaryVO列表 */
    List<CustomerArchiveSummaryVO> toSummaryList(List<CstCompanyArchive> entities);

    /** 实体 → ViewVO */
    CustomerArchiveViewVO toView(CstCompanyArchive entity);

    /** 审核日志实体 → ViewVO.AuditLogVO */
    CustomerArchiveViewVO.AuditLogVO toAuditLogVO(CstArchiveAuditLog log);

    /** 审核日志列表 → ViewVO.AuditLogVO列表 */
    List<CustomerArchiveViewVO.AuditLogVO> toAuditLogVOList(List<CstArchiveAuditLog> logs);

    /** 用户绑定实体 → ViewVO.BoundUserVO */
    CustomerArchiveViewVO.BoundUserVO toBoundUserVO(CstArchiveUserBinding binding);

    /** 用户绑定列表 → ViewVO.BoundUserVO列表 */
    List<CustomerArchiveViewVO.BoundUserVO> toBoundUserVOList(List<CstArchiveUserBinding> bindings);

    @Mapping(target = "customerName", source = "companyName")
    CustomerContext toContext(CstCompanyArchive entity);
}
