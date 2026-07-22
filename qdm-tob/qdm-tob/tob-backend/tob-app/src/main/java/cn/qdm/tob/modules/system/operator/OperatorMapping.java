package cn.qdm.tob.modules.system.operator;

import cn.qdm.tob.modules.system.operator.domain.SysOperator;
import cn.qdm.tob.modules.system.operator.dto.OperatorSaveDTO;
import cn.qdm.tob.modules.system.operator.vo.OperatorVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * 运营人员模块对象映射器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OperatorMapping {

    /** OperatorSaveDTO → SysOperator 实体 */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    SysOperator toEntity(OperatorSaveDTO dto);

    /** SysOperator 实体 → OperatorVO（手机号脱敏由 Service 处理） */
    OperatorVO toSummary(SysOperator entity);
}
