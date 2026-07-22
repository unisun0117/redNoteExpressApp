package cn.qdm.tob.modules.order.account;

import cn.qdm.tob.modules.order.account.domain.OrdCustomerAccount;
import cn.qdm.tob.modules.order.account.vo.AccountSummaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 账户管理 MapStruct 对象映射器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapping {

    /** 账户实体 → ViewVO（客户字段由 Service 层填充） */
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "licenseNo", ignore = true)
    @Mapping(target = "companyName", ignore = true)
    @Mapping(target = "licensePhoto", ignore = true)
    AccountSummaryVO toSummary(OrdCustomerAccount entity);

    /** 批量转换 */
    List<AccountSummaryVO> toSummaryList(List<OrdCustomerAccount> entities);
}
