package cn.qdm.tob.modules.customer.cst.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 小程序端 - 提交/编辑公司收货地址入参
 */
@Data
@Schema(description = "提交/编辑公司收货地址")
public class CustomerAddressSubmitVO {

    @Schema(description = "公司名称")
    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    @Schema(description = "营业执照编号")
    @NotBlank(message = "营业执照编号不能为空")
    private String licenseNo;

    @Schema(description = "门头照URL（OSS暂未完成，暂不强制）")
    private String doorPhoto;

    @Schema(description = "营业执照照URL（OSS暂未完成，暂不强制）")
    private String licensePhoto;

    @Schema(description = "收货人姓名")
    @NotBlank(message = "收货人姓名不能为空")
    private String contactName;

    @Schema(description = "收货人联系电话")
    @NotBlank(message = "收货人联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    @Schema(description = "所在省份")
    @NotBlank(message = "所在省份不能为空")
    private String province;

    @Schema(description = "所在城市")
    @NotBlank(message = "所在城市不能为空")
    private String city;

    @Schema(description = "所在区县")
    @NotBlank(message = "所在区县不能为空")
    private String district;

    @Schema(description = "详细地址")
    @NotBlank(message = "详细地址不能为空")
    private String address;

    @Schema(description = "经度")
    private String longitude;

    @Schema(description = "纬度")
    private String latitude;

    @Schema(description = "可收货时段开始")
    @NotBlank(message = "可收货时段开始不能为空")
    private String receiveTimeStart;

    @Schema(description = "可收货时段结束")
    @NotBlank(message = "可收货时段结束不能为空")
    private String receiveTimeEnd;

    @Schema(description = "收货要求")
    private String receiveRequirement;

    @Schema(description = "收货存放位置照片（URL数组）")
    private List<String> storagePhotos;

    @Schema(description = "业务员推荐码")
    private String referralCode;
}
