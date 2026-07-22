package cn.qdm.tob.modules.customer.cst.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.customer.cst.domain.CstArchiveUserBinding;
import org.apache.ibatis.annotations.Mapper;

/**
 * 档案用户绑定 Mapper
 */
@Mapper
public interface ArchiveUserBindingMapper extends TobBaseMapper<CstArchiveUserBinding> {
}
