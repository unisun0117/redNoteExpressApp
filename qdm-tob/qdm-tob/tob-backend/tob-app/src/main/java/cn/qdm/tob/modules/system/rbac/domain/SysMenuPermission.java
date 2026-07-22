package cn.qdm.tob.modules.system.rbac.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 按钮-权限关联
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_menu_permission")
public class SysMenuPermission {
    private Long menuId;

    private String permissionCode;

    /** 创建人（操作人姓名） */
    private String createdBy;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
