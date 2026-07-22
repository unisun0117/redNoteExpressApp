package cn.qdm.tob.modules.order.account.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 流水类型
 */
@Getter
@RequiredArgsConstructor
public enum TransactionType implements Describable {

    /** 充值 */
    RECHARGE("充值"),

    /** 提现 */
    WITHDRAW("提现"),

    /** 下单 */
    ORDER("下单"),

    /** 退款 */
    REFUND("退款");

    private final String description;
}
