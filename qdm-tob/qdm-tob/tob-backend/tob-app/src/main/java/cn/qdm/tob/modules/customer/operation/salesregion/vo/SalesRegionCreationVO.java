package cn.qdm.tob.modules.customer.operation.salesregion.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增大区入参 VO
 */
@Data
@Schema(description = "新增大区")
public class SalesRegionCreationVO {

    @Schema(description = "销售大区编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "编号不能为空")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "仅支持大写字母和数字")
    private String code;

    @Schema(description = "销售大区名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "下单服务启用", defaultValue = "true")
    private Boolean serviceEnabled = true;

    @Schema(description = "多天订购", defaultValue = "false")
    private Boolean multiDay = false;

    @Schema(description = "最少订购天数")
    private Integer minDays;

    @Schema(description = "营业时间")
    private String bizHours;

    @Schema(description = "起订类型", defaultValue = "按金额")
    private String orderType = "按金额";

    @Schema(description = "起订金额/重量")
    private String orderAmount;

    @Schema(description = "到货日期 T+N", defaultValue = "1")
    private Integer arrivalDays = 1;

    @Schema(description = "价格审批", defaultValue = "false")
    private Boolean priceApproval = false;

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
    @NotBlank(message = "商户号不能为空")
    private String merchantNo;

    @Schema(description = "区域经理列表 JSON")
    private String regionManagers;

    @Schema(description = "覆盖城市列表 JSON")
    private String coveredCities;

    @Schema(description = "商户号名称")
    private String merchantName;

    @Schema(description = "创建人")
    private String createdBy;
}
