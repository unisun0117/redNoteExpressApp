package cn.qdm.tob.modules.product.pricing.domain;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;

/**
 * 审批状态枚举
 */
@Getter
public enum ApprovalStatus implements Describable {

    /** 无需审批 */
    NONE("无需审批"),

    /** 审批中 */
    PENDING("审批中"),

    /** 审批通过 */
    APPROVED("审批通过"),

    /** 审批驳回 */
    REJECTED("审批驳回");

    private final String description;

    ApprovalStatus(String description) {
        this.description = description;
    }
}
