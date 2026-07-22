package cn.qdm.tob.modules.customer.cst.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台新增客户档案入参VO
 */
@Data
@Schema(description = "后台新增客户档案")
public class CustomerArchiveCreateVO {

    @Schema(description = "公司名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    @Schema(description = "营业执照编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "营业执照编号不能为空")
    private String licenseNo;

    @Schema(description = "门头照URL")
    private String doorPhoto;

    @Schema(description = "营业执照照URL")
    private String licensePhoto;

    @Schema(description = "收货人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收货人姓名不能为空")
    private String contactName;

    @Schema(description = "收货人联系电话", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收货人联系电话不能为空")
    private String contactPhone;

    @Schema(description = "所在省份", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "所在省份不能为空")
    private String province;

    @Schema(description = "所在城市", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "所在城市不能为空")
    private String city;

    @Schema(description = "所在区县", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "所在区县不能为空")
    private String district;

    @Schema(description = "详细收货地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "详细收货地址不能为空")
    private String address;

    @Schema(description = "经度")
    private String longitude;

    @Schema(description = "纬度")
    private String latitude;

    @Schema(description = "可收货时段开始", defaultValue = "00:00")
    private String receiveTimeStart = "00:00";

    @Schema(description = "可收货时段结束", defaultValue = "08:00")
    private String receiveTimeEnd = "08:00";

    @Schema(description = "收货要求")
    private String receiveRequirement;

    @Schema(description = "销售大区名称")
    private String salesRegionName;
}
