package cn.qdm.tob.modules.order.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客户信息出参
 */
@Data
@Schema(description = "客户信息")
public class CustomerInfoVO {

    @Schema(description = "客户编码")
    private String customerCode;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "营业执照编号")
    private String licenseNo;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "营业执照照片URL")
    private String licensePhoto;
}
