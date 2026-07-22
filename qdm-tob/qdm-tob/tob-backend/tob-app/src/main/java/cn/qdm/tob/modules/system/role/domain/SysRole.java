package cn.qdm.tob.modules.system.role.domain;

import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.infrastructure.base.TobBaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_role")
public class SysRole extends TobBaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色码，如 ADMIN/SALESMAN */
    private String code;

    /** 角色显示名，如 管理员 */
    private String name;

    private String description;

    private RecordStatus status;
}
