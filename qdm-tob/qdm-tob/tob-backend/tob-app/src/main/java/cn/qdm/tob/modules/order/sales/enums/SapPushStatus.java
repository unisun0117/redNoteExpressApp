package cn.qdm.tob.modules.order.sales.enums;

import cn.qdm.tob.framework.Describable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SAP推送状态枚举
 */
@Getter
@AllArgsConstructor
public enum SapPushStatus implements Describable {

    /** 待推送 */
    PENDING("待推送"),
    /** 已推送 */
    PUSHED("已推送"),
    /** 推送失败 */
    FAILED("推送失败");

    private final String description;
}
