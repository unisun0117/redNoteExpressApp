package cn.qdm.tob.modules.system.privacy.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;

/**
 * 隐私文档状态
 * <p>状态机：UNPUBLISHED（未发布）→ PUBLISHED（已发布）→ WITHDRAWN（已下架，终态）</p>
 */
@Getter
public enum DocStatus implements Describable {

    UNPUBLISHED("未发布"),
    PUBLISHED("已发布"),
    WITHDRAWN("已下架");

    private final String description;

    DocStatus(String description) {
        this.description = description;
    }
}
