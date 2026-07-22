package cn.qdm.tob.modules.customer.cst.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户档案列表视图对象
 */
@Data
@Schema(description = "客户档案列表项")
public class CustomerArchiveSummaryVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "SAP客户编码")
    private String sapCustomerCode;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "收货人姓名")
    private String contactName;

    @Schema(description = "收货人联系电话")
    private String contactPhone;

    @Schema(description = "所在省份")
    private String province;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "所在区县")
    private String district;

    @Schema(description = "详细收货地址")
    private String address;

    @Schema(description = "归属销售大区名称")
    private String salesRegionName;

    @Schema(description = "归属业务员姓名")
    private String salesmanName;

    @Schema(description = "审核状态：PENDING/APPROVED/REJECTED")
    private String auditStatus;

    @Schema(description = "价格组")
    private String priceGroup;

    @Schema(description = "结算公司")
    private String settleCompany;

    @Schema(description = "经营类型")
    private String businessType;

    @Schema(description = "结算类型：CASH/PERIOD")
    private String settleType;

    @Schema(description = "审核处理人姓名")
    private String auditorName;

    @Schema(description = "最近下单时间")
    private LocalDateTime lastOrderTime;

    @Schema(description = "创建/提交时间")
    private LocalDateTime createdAt;

    @Schema(description = "已绑定用户数量")
    private Integer boundUserCount;
}
