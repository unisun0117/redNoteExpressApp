package cn.qdm.tob.modules.system.operator.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 运营人员-销售大区绑定实体，对应表 sys_operator_region。
 * <p>一个运营人员可绑定多个销售大区，绑定关系由 {@link cn.qdm.tob.modules.system.operator.service.OperatorRegionService} 覆盖式管理。</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_operator_region")
public class SysOperatorRegion {
    /** 运营人员 ID，关联 sys_operator.id */
    private Long operatorId;
    /** 销售大区编号，关联 sys_sales_region.code */
    private String regionCode;
    /** 创建人（操作人姓名） */
    private String createdBy;
    /** 绑定时间 */
    private LocalDateTime createdAt;
}
