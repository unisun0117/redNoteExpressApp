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
 * API ↔ 权限映射实体（全动态 URL 授权）
 *
 * @deprecated 当前版本使用基于菜单按钮的静态权限码（{@code sys_menu_permission}），
 *             通过 {@link cn.qdm.tob.infrastructure.security.RequirePermission @RequirePermission} 注解校验，
 *             而非动态 URL 匹配。本类及相关链路保留供后续按需启用。
 */
@Deprecated
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_api_permission")
public class ApiPermission {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** HTTP 方法 GET/POST/PUT/DELETE */
    private String httpMethod;

    /** URL 匹配模式，Ant 风格，如 /api/order/** */
    private String urlPattern;

    /** 所需权限码，如 order:view */
    private String permissionCode;

    private String description;

    private RecordStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
