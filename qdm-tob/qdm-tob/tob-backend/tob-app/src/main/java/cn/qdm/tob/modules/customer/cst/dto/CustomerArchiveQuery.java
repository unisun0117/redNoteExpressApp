package cn.qdm.tob.modules.customer.cst.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客户档案查询DTO
 */
@Data
@Schema(description = "客户档案查询条件")
public class CustomerArchiveQuery {

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 20;

    @Schema(description = "公司名称（模糊搜索）")
    private String companyName;

    @Schema(description = "销售大区ID")
    private Long salesRegionId;

    @Schema(description = "销售大区名称")
    private String salesRegionName;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区县")
    private String district;

    @Schema(description = "业务员姓名（模糊搜索）")
    private String salesmanName;

    @Schema(description = "审核状态：PENDING/APPROVED/REJECTED")
    private String auditStatus;

    @Schema(description = "审核人姓名（模糊搜索）")
    private String auditorName;

    @Schema(description = "注册时间起始（YYYY-MM-DD）")
    private String startDate;

    @Schema(description = "注册时间结束（YYYY-MM-DD）")
    private String endDate;
}
