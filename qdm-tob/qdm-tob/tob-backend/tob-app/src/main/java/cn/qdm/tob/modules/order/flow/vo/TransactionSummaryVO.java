package cn.qdm.tob.modules.order.flow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 资金流水汇总统计
 */
@Data
@Schema(description = "资金流水汇总统计")
public class TransactionSummaryVO {

    @Schema(description = "收入总金额")
    private BigDecimal incomeAmount;

    @Schema(description = "收入笔数")
    private long incomeCount;

    @Schema(description = "支出总金额")
    private BigDecimal expenseAmount;

    @Schema(description = "支出笔数")
    private long expenseCount;
}
