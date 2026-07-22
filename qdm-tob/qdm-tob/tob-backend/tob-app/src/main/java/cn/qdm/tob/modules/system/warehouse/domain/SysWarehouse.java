package cn.qdm.tob.modules.system.warehouse.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仓库档案实体
 */
@Data
@TableName("sys_warehouse")
public class SysWarehouse {

    /** 主键 */
    @TableId
    private Long id;

    /** 仓库编码（唯一索引） */
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

    /** 创建人 */
    private String createdBy;

    /** 修改人 */
    private String updatedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 修改时间 */
    private LocalDateTime updatedAt;
}
