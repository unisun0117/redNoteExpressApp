package cn.qdm.tob.modules.customer.operation.salesregion.api.internal;

import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.dto.SalesRegionDetailDTO;
import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.dto.SalesRegionSimpleDTO;

import java.util.List;

/**
 * 销售大区内部 API，供数据权限等模块跨模块调用
 */
public interface SalesRegionApi {
    /** 获取所有销售大区（id + name），供数据权限配置使用 */
    List<SalesRegionSimpleDTO> listAll();

    /** 按编号查询销售大区详情（含审批配置），供价格管理模块使用 */
    SalesRegionDetailDTO getDetailByCode(String code);

    /** 查询销售大区详情（含审批配置），供价格管理模块使用 */
    SalesRegionDetailDTO getDetailById(Long id);
}
