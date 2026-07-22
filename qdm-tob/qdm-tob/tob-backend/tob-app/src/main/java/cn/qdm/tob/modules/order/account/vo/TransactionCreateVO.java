package cn.qdm.tob.modules.order.account.vo;

import cn.qdm.tob.modules.order.account.enums.AccountType;
import cn.qdm.tob.modules.order.account.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增流水的入参
 */
@Data
@Schema(description = "新增流水")
public class TransactionCreateVO {

    @Schema(description = "客户编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "客户编码不能为空")
    private String customerCode;

    @Schema(description = "账户类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "账户类型不能为空")
    private AccountType accountType;

    @Schema(description = "流水类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "流水类型不能为空")
    private TransactionType transactionType;

    @Schema(description = "流水金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "流水金额不能为空")
    @DecimalMin(value = "0.01", message = "流水金额必须大于0")
    private BigDecimal amount;
}
