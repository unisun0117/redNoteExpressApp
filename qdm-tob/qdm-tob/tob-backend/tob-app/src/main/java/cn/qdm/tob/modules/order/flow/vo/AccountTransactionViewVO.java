package cn.qdm.tob.modules.order.flow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金流水列表出参（14列）
 */
@Data
@Schema(description = "资金流水记录")
public class AccountTransactionViewVO {

    @Schema(description = "流水ID")
    private Long id;

    @Schema(description = "流水号")
    private String transactionNo;

    @Schema(description = "入账时间")
    private LocalDateTime createdAt;

    @Schema(description = "账务类型")
    private String transactionType;

    @Schema(description = "付款方式")
    private String paymentMethod;

    @Schema(description = "客户编号")
    private String customerCode;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "结算账户编号")
    private String settlementAccountCode;

    @Schema(description = "结算账户名称")
    private String settlementAccountName;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "收支类型：INCOME/EXPENSE")
    private String incomeExpenseType;

    @Schema(description = "收支金额")
    private BigDecimal amount;

    @Schema(description = "账户余额")
    private BigDecimal balanceAfter;

    @Schema(description = "业务单号")
    private String businessNo;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "第三方流水号")
    private String thirdPartyFlowNo;

    @Schema(description = "交易状态")
    private String status;

    @Schema(description = "备注")
    private String remark;
}
