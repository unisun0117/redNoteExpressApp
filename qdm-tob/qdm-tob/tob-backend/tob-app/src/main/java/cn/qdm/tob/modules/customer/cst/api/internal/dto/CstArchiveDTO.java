package cn.qdm.tob.modules.customer.cst.api.internal.dto;

import lombok.Data;

/**
 * 客户档案 DTO，供订单模块跨模块查询
 */
@Data
public class CstArchiveDTO {

    /** 档案ID */
    private Long id;

    /** 公司名称 */
    private String companyName;

    /** SAP客户编码 */
    private String sapCustomerCode;

    /** 归属销售大区ID */
    private Long salesRegionId;

    /** 归属销售大区名称 */
    private String salesRegionName;

    /** 价格组 */
    private String priceGroup;

    /** 结算类型：CASH/PERIOD */
    private String settleType;

    /** 结算公司 */
    private String settleCompany;

    /** 审核状态：PENDING/APPROVED/REJECTED */
    private String auditStatus;

    /** 收货人姓名 */
    private String contactName;

    /** 收货人联系电话 */
    private String contactPhone;

    /** 详细收货地址 */
    private String address;

    /** 所在省份 */
    private String province;

    /** 所在城市 */
    private String city;

    /** 所在区县 */
    private String district;

    /** 归属业务员ID */
    private Long salesmanId;

    /** 归属业务员姓名 */
    private String salesmanName;

    /** 营业执照编号 */
    private String licenseNo;

    /** 营业执照照片URL */
    private String licensePhoto;
}
