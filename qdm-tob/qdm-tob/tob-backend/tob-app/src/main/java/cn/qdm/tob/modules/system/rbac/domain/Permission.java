package cn.qdm.tob.modules.system.rbac.domain;

import cn.qdm.tob.framework.model.RecordStatus;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限实体（旧链路 sys_permission 表）
 *
 * @deprecated 权限校验已迁移至 {@code sys_menu_permission}（菜单按钮权限码），
 *             见 {@link cn.qdm.tob.modules.system.rbac.service.OperatorAuthorityLoader}。
 *             本类及相关链路保留供后续按需启用。
 */
@Deprecated
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_permission")
public class Permission {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 权限码，如 order:view */
    private String code;

    /** 权限显示名，如 查看订单 */
    private String name;

    private String description;

    /** 资源，如 order */
    private String resource;

    /** 操作，如 view/edit/delete */
    private String action;

    private RecordStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
