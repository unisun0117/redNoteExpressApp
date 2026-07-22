package cn.qdm.tob.modules.order.flow.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 付款方式
 */
@Getter
@RequiredArgsConstructor
public enum PaymentMethod implements Describable {

    WECHAT("微信"),
    PREPAID("预付款"),
    CREDIT("账期"),
    BANK_CARD("银行卡");

    private final String description;
}
