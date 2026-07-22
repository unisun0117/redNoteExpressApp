package cn.qdm.tob.modules.order.account.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 账户类型
 */
@Getter
@RequiredArgsConstructor
public enum AccountType implements Describable {

    /** 预付款 — 先充值后消费 */
    PREPAID("预付款"),

    /** 账期 — 先消费后结算 */
    CREDIT("账期");

    private final String description;
}
