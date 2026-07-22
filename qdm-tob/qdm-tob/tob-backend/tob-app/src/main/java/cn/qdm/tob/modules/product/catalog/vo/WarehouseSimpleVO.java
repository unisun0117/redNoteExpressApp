package cn.qdm.tob.modules.product.catalog.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 仓库简要信息 VO（下拉选项）
 */
@Data
@Builder
public class WarehouseSimpleVO {

    private String code;
    private String name;
    private String region;
}
