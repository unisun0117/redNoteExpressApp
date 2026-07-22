package cn.qdm.tob.modules.system.user.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;

/**
 * 用户状态（对应 user.status）
 */
@Getter
public enum UserStatus implements Describable {

    ACTIVE("正常"),
    INACTIVE("禁用"),
    FROZEN("冻结");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }
}
