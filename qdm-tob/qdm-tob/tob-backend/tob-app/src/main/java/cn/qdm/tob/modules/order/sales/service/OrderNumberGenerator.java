package cn.qdm.tob.modules.order.sales.service;

import cn.qdm.tob.modules.system.sequence.api.internal.SequenceApi;
import cn.qdm.tob.modules.system.sequence.api.internal.dto.SequenceResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 订单编号生成器，封装 SequenceApi 的订单号生成逻辑
 */
@Component
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private final SequenceApi sequenceApi;

    /**
     * 生成下一个订单编号
     *
     * @return 格式化后的订单编号，如 "202607080001"
     */
    public String nextOrderNo() {
        SequenceResult result = sequenceApi.next("ORDER_NO");
        return result.getFormattedValue();
    }
}
