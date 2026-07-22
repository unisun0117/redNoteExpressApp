package cn.qdm.tob.modules.system.operator.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;

/**
 * 管理员内置角色（对应 admin.role，遗留字段，RBAC 体系由 operator_role 表管理）
 */
@Getter
public enum OperatorType implements Describable {

    ADMIN("管理员"),
    SALESMAN("销售员");

    private final String description;

    OperatorType(String description) {
        this.description = description;
    }
}
