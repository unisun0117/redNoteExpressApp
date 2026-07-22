package cn.qdm.tob.infrastructure.security.resolver;

import lombok.Data;

/**
 * 当前选中客户上下文 — 由 CurrentCustomerArgumentResolver 注入
 */
@Data
public class CustomerContext {
    /** 客户档案ID（即 addressId） */
    private Long id;
    private String sapCustomerCode;
    /** 客户名称 */
    private String customerName;
    /** 销售大区ID */
    private Long salesRegionId;
    /** 销售大区编号（用于查商品/价格） */
    private String salesRegionCode;
    /** 价格组编码 */
    private String priceGroup;
    /** 结算类型 */
    private String settleType;
    /** 收货人 */
    private String contactName;
    /** 收货电话 */
    private String contactPhone;
    /** 收货地址 */
    private String address;
    /** 业务员ID */
    private Long salesmanId;
}
