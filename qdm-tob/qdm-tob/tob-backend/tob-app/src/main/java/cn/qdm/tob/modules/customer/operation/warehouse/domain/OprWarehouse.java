package cn.qdm.tob.modules.customer.operation.warehouse.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_warehouse")
public class OprWarehouse {
    @TableId private Long id;
    private String code;
    private String name;
    private String region;
    private String type;
    private String province;
    private String provinceName;
    private String cityName;
    private String districtName;
    private String city;
    private String district;
    private String address;
    private BigDecimal lng;
    private BigDecimal lat;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
