package cn.qdm.tob.modules.customer.cst.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 小程序端 - 公司收货地址详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公司收货地址详情")
public class CustomerAddressDetailVO {

    @Schema(description = "地址ID")
    private Long id;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "门头照URL")
    private String doorPhoto;

    @Schema(description = "营业执照编号")
    private String licenseNo;

    @Schema(description = "营业执照照URL")
    private String licensePhoto;

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

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "完整地址（省市区+详细地址）")
    private String fullAddress;

    @Schema(description = "可收货时段开始")
    private String receiveTimeStart;

    @Schema(description = "可收货时段结束")
    private String receiveTimeEnd;

    @Schema(description = "收货要求")
    private String receiveRequirement;

    @Schema(description = "收货存放位置照片")
    private List<String> storagePhotos;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "驳回原因")
    private String auditRejectReason;

    @Schema(description = "当前用户是否是地址管理员")
    private Boolean isAdmin;
}
