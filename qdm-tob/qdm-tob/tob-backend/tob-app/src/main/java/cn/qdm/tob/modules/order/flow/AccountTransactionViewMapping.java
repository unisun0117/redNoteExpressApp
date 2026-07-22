package cn.qdm.tob.modules.order.flow;

import cn.qdm.tob.modules.order.account.domain.OrdAccountTransaction;
import cn.qdm.tob.modules.order.flow.vo.AccountTransactionViewVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 资金流水 MapStruct 映射器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountTransactionViewMapping {

    /** Entity → ViewVO */
    AccountTransactionViewVO toView(OrdAccountTransaction entity);

    /** 批量 Entity → ViewVO */
    List<AccountTransactionViewVO> toViewList(List<OrdAccountTransaction> entities);
}
