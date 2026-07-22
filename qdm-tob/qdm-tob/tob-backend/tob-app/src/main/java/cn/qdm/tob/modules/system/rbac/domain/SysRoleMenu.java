package cn.qdm.tob.modules.system.rbac.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色-菜单关联
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_role_menu")
public class SysRoleMenu {
    private Long roleId;

    private Long menuId;

    /** 创建人（操作人姓名） */
    private String createdBy;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
