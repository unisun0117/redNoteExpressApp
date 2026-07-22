package cn.qdm.tob.modules.customer.operation.salesman.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 业务员月度绩效实体
 */
@Data
@TableName("sys_salesman_performance")
public class OprSalesmanPerformance {

    @TableId
    private Long id;

    /** 关联业务员ID */
    private Long salesmanId;

    /** 销售员名称（冗余存储，避免跨表查询） */
    private String salesmanName;

    /** 月度（YYYY-MM） */
    private String month;

    /** 订单数 */
    private Integer orderCount;

    /** 下单客户数 */
    private Integer customerCount;

    /** 销售额（元） */
    private BigDecimal salesAmount;

    /** 创建人 */
    private String createdBy;

    /** 修改人 */
    private String updatedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 修改时间 */
    private LocalDateTime updatedAt;
}
