package cn.qdm.tob.modules.order.account.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交易状态
 */
@Getter
@RequiredArgsConstructor
public enum TransactionStatus implements Describable {

    /** 处理中 */
    PROCESSING("处理中"),

    /** 成功 */
    SUCCESS("成功"),

    /** 失败 */
    FAILED("失败");

    private final String description;
}
