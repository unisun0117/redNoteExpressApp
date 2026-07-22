package cn.qdm.tob.modules.customer.cst.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小程序端 - 公司收货地址列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公司收货地址列表项")
public class CustomerAddressListVO {

    @Schema(description = "地址ID")
    private Long id;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "收货人姓名")
    private String contactName;

    @Schema(description = "收货人联系电话（已脱敏）")
    private String contactPhone;

    @Schema(description = "所在省份")
    private String province;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "所在区县")
    private String district;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "完整地址（省市区+详细地址）")
    private String fullAddress;

    @Schema(description = "审核状态：PENDING/APPROVED/REJECTED")
    private String auditStatus;

    @Schema(description = "驳回原因")
    private String auditRejectReason;

    @Schema(description = "提交时间")
    private String createdAt;

    @Schema(description = "当前用户是否是地址管理员")
    private Boolean isAdmin;
}
