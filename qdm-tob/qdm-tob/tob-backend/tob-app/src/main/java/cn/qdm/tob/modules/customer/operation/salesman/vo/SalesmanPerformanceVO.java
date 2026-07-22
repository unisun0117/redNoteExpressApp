package cn.qdm.tob.modules.customer.operation.salesman.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 业务员绩效 出参 / 入参共用
 * 注意：id / salesmanId 使用 String 类型，避免 Snowflake Long 在 JS 端精度丢失
 */
@Data
@Schema(description = "业务员绩效")
public class SalesmanPerformanceVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "业务员ID")
    @NotBlank(message = "请选择业务员")
    private String salesmanId;

    @Schema(description = "业务员姓名")
    private String salesmanName;

    @Schema(description = "月度（YYYY-MM）")
    @NotBlank(message = "请输入月度")
    private String month;

    @Schema(description = "订单数")
    @NotNull(message = "请输入订单数")
    private Integer orderCount;

    @Schema(description = "下单客户数")
    @NotNull(message = "请输入下单客户数")
    private Integer customerCount;

    @Schema(description = "销售额（元）")
    @NotNull(message = "请输入销售额")
    private BigDecimal salesAmount;

    @Schema(description = "修改人")
    private String updatedBy;

    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;
}
