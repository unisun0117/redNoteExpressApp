package cn.qdm.tob.modules.customer.operation.salesregion.api.internal.dto;

import lombok.Data;

/**
 * 销售大区精简 DTO，供内部跨模块调用使用。
 * <p>调用方通过 {@link cn.qdm.tob.modules.customer.operation.salesregion.api.internal.SalesRegionApi#listAll()} 获取。</p>
 * <p>前端通过 code 绑定，避免 Long 精度问题。</p>
 */
@Data
public class SalesRegionSimpleDTO {
    /** 销售大区 ID */
    private Long id;
    /** 销售大区编号（业务主键） */
    private String code;
    /** 销售大区名称 */
    private String name;
}
