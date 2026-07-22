package cn.qdm.tob.modules.customer.cst.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 编辑业务属性入参VO
 */
@Data
@Schema(description = "编辑业务属性（含收货人信息）")
public class CustomerArchiveEditVO {

    @Schema(description = "收货人姓名")
    private String contactName;

    @Schema(description = "收货人联系电话")
    private String contactPhone;

    @Schema(description = "价格组")
    private String priceGroup;

    @Schema(description = "结算公司")
    private String settleCompany;

    @Schema(description = "经营类型")
    private String businessType;

    @Schema(description = "结算类型：CASH/PERIOD")
    private String settleType;

    @Schema(description = "内部收货备注")
    private String internalRemark;
}
