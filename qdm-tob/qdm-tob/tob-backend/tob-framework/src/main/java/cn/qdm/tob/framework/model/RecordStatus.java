package cn.qdm.tob.framework.model;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;

/**
 * 通用启用/禁用状态
 */
@Getter
public enum RecordStatus implements Describable {

    ACTIVE("启用"),
    INACTIVE("禁用");

    private final String description;

    RecordStatus(String description) {
        this.description = description;
    }
}
