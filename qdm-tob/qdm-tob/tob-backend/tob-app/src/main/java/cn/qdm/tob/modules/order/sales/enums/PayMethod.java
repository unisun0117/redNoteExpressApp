package cn.qdm.tob.modules.order.sales.enums;

import cn.qdm.tob.framework.Describable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
@AllArgsConstructor
public enum PayMethod implements Describable {

    /** 微信 */
    WECHAT("微信"),
    /** 账期 */
    CREDIT("账期"),
    /** 余额 */
    BALANCE("余额");

    private final String description;
}
