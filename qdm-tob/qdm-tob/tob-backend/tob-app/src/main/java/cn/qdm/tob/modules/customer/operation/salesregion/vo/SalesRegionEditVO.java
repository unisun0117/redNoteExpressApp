package cn.qdm.tob.modules.customer.operation.salesregion.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 编辑大区入参 VO（编号不可修改，通过 Query 参数传递）
 */
@Data
@Schema(description = "编辑大区")
public class SalesRegionEditVO {

    @Schema(description = "销售大区编号（只读，不可修改）", hidden = true)
    private String code;

    @Schema(description = "销售大区名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "下单服务启用")
    private Boolean serviceEnabled;

    @Schema(description = "多天订购")
    private Boolean multiDay;

    @Schema(description = "最少订购天数")
    private Integer minDays;

    @Schema(description = "营业时间")
    private String bizHours;

    @Schema(description = "起订类型")
    private String orderType;

    @Schema(description = "起订金额/重量")
    private String orderAmount;

    @Schema(description = "到货日期 T+N")
    private Integer arrivalDays;

    @Schema(description = "价格审批")
    private Boolean priceApproval;

    @Schema(description = "审批阈值(%)")
    private BigDecimal approvalThreshold;

    @Schema(description = "审批人列表 JSON")
    private String approvers;

    @Schema(description = "标准物流费")
    private BigDecimal stdFreight;

    @Schema(description = "免运费金额")
    private BigDecimal stdFreeAmount;

    @Schema(description = "新客物流费")
    private BigDecimal newFreight;

    @Schema(description = "新客免运费金额")
    private BigDecimal newFreeAmount;

    @Schema(description = "商户号")
    private String merchantNo;

    @Schema(description = "区域经理列表 JSON")
    private String regionManagers;

    @Schema(description = "覆盖城市列表 JSON")
    private String coveredCities;

    @Schema(description = "商户号名称")
    private String merchantName;

    @Schema(description = "修改人")
    private String updatedBy;
}
