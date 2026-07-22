package cn.qdm.tob.modules.customer.cst.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.customer.cst.domain.CstArchiveAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核历史记录 Mapper
 */
@Mapper
public interface ArchiveAuditLogMapper extends TobBaseMapper<CstArchiveAuditLog> {
}
