package cn.qdm.tob.modules.customer.operation.salesregion.api.internal.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售大区详情 DTO（含审批配置），供价格管理等跨模块调用使用
 */
@Data
public class SalesRegionDetailDTO {
    /** 销售大区 ID */
    private Long id;
    /** 销售大区编号（业务主键） */
    private String code;
    /** 销售大区名称 */
    private String name;
    /** 是否开启价格审批 */
    private Boolean priceApproval;
    /** 审批阈值（%） */
    private BigDecimal approvalThreshold;

    /** 标准物流费 */
    private BigDecimal stdFreight;

    /** 免运费金额 */
    private BigDecimal stdFreeAmount;
}
