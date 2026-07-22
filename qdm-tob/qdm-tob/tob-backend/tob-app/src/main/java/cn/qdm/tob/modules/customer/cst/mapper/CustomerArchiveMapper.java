package cn.qdm.tob.modules.customer.cst.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.customer.cst.domain.CstCompanyArchive;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户档案 Mapper
 */
@Mapper
public interface CustomerArchiveMapper extends TobBaseMapper<CstCompanyArchive> {
}
