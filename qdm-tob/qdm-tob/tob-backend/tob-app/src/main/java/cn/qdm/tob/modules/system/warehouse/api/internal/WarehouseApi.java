package cn.qdm.tob.modules.system.warehouse.api.internal;

import cn.qdm.tob.modules.system.warehouse.api.internal.dto.WarehouseDTO;

import java.util.List;

/**
 * 仓库查询 API（供其他模块调用）。
 * <p>
 * 其他业务模块应通过此接口获取仓库信息，不可直接引用 warehouse 模块内部的 service / domain / mapper。
 */
public interface WarehouseApi {

    /**
     * 按销售大区编码查询仓库列表。
     *
     * @param region 销售大区编码，传 null 查询全部
     * @return 仓库列表
     */
    List<WarehouseDTO> listByRegion(String region);

    /**
     * 按仓库编码查询单个仓库。
     *
     * @param code 仓库编码
     * @return 仓库信息，查不到返回 null
     */
    WarehouseDTO getByCode(String code);
}
