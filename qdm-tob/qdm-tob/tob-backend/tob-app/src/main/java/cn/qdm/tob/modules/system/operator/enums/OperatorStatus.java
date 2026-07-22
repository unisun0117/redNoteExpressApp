package cn.qdm.tob.modules.system.operator.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;

/**
 * 管理员状态（对应 admin.status）
 */
@Getter
public enum OperatorStatus implements Describable {

    ACTIVE("正常"),
    INACTIVE("禁用"),
    LOCKED("锁定");

    private final String description;

    OperatorStatus(String description) {
        this.description = description;
    }
}
