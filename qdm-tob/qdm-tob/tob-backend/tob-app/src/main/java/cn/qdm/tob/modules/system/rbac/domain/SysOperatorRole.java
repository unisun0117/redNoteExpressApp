package cn.qdm.tob.modules.system.rbac.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员-角色关系实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_operator_role")
public class SysOperatorRole {
    private Long operatorId;

    private Long roleId;

    /** 创建人（操作人姓名） */
    private String createdBy;

    private LocalDateTime createdAt;
}
