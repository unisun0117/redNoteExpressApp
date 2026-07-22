package cn.qdm.tob.modules.system.operator.mapper;

import cn.qdm.tob.modules.system.operator.enums.OperatorStatus;
import cn.qdm.tob.modules.system.operator.domain.SysOperator;
import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SysOperatorMapper extends TobBaseMapper<SysOperator> {

    default Optional<SysOperator> findByMobile(String mobile) {
        return lambdaSelectOne(w -> w.eq(SysOperator::getMobile, mobile));
    }

    default Optional<SysOperator> findByEmployeeCode(String employeeCode) {
        return lambdaSelectOne(w -> w.eq(SysOperator::getEmployeeCode, employeeCode));
    }

    default Optional<SysOperator> findByLoginId(String loginId) {
        return lambdaSelectOne(w -> w.eq(SysOperator::getLoginId, loginId));
    }

    /** 分页搜索运营人员（XML 实现，避免动态拼接） */
    Page<SysOperator> pageSearch(Page<SysOperator> page,
                                  @Param("employeeCode") String employeeCode,
                                  @Param("realName") String realName,
                                  @Param("mobile") String mobile,
                                  @Param("status") String status,
                                  @Param("type") String type);

    /** 查询所有停用/锁定的操作员 ID（用于初始化 Redis 黑名单） */
    default List<Long> findAllInactiveIds() {
        LambdaQueryWrapper<SysOperator> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysOperator::getId)
                .in(SysOperator::getStatus, OperatorStatus.INACTIVE, OperatorStatus.LOCKED);
        return selectList(wrapper).stream()
                .map(SysOperator::getId)
                .toList();
    }
}
