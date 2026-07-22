package cn.qdm.tob.modules.system.warehouse.api.internal.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 仓库信息 DTO（跨模块调用使用）
 */
@Data
@Builder
public class WarehouseDTO {

    /** 仓库编码 */
    private String code;

    /** 仓库名称 */
    private String name;

    /** 销售大区编码 */
    private String region;

    /** 仓库性质 */
    private String type;

    /** 省 */
    private String province;

    /** 市 */
    private String city;

    /** 区 */
    private String district;

    /** 详细地址 */
    private String address;

    /** 经度 */
    private BigDecimal lng;

    /** 纬度 */
    private BigDecimal lat;

    /** 省名称 */
    private String provinceName;

    /** 市名称 */
    private String cityName;

    /** 区名称 */
    private String districtName;
}
