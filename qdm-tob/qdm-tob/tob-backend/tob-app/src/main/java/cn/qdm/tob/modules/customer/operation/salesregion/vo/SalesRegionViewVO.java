package cn.qdm.tob.modules.customer.operation.salesregion.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售大区列表/详情出参 VO
 */
@Data
@Schema(description = "销售大区")
public class SalesRegionViewVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "销售大区编号")
    private String code;

    @Schema(description = "销售大区名称")
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

    @Schema(description = "到货日期")
    private Integer arrivalDays;

    @Schema(description = "价格审批")
    private Boolean priceApproval;

    @Schema(description = "审批阈值(%)")
    private BigDecimal approvalThreshold;

    @Schema(description = "审批人列表 JSON")
    private String approvers;

    @Schema(description = "已关联仓库数")
    private Integer warehouseCount;

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

    @Schema(description = "商户号名称")
    private String merchantName;

    @Schema(description = "区域经理列表 JSON")
    private String regionManagers;

    @Schema(description = "覆盖城市列表 JSON")
    private String coveredCities;

    @Schema(description = "修改人")
    private String updatedBy;

    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
