package cn.qdm.tob.modules.customer.operation.salesregion.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售大区实体
 */
@Data
@TableName("sys_sales_region")
public class OprSalesRegion {

    @TableId
    private Long id;

    /** 销售大区编号 */
    private String code;

    /** 销售大区名称 */
    private String name;

    /** 下单服务启用 1=启用 0=停用 */
    private Boolean serviceEnabled;

    /** 多天订购 1=开启 0=关闭 */
    private Boolean multiDay;

    /** 最少订购天数 */
    private Integer minDays;

    /** 营业时间 */
    private String bizHours;

    /** 起订类型 */
    private String orderType;

    /** 起订金额/重量 */
    private String orderAmount;

    /** 到货日期 T+N */
    private Integer arrivalDays;

    /** 价格审批 1=开启 0=关闭 */
    private Boolean priceApproval;

    /** 审批阈值(%) */
    private BigDecimal approvalThreshold;

    /** 审批人列表 JSON */
    private String approvers;

    /** 已关联仓库数 */
    private Integer warehouseCount;

    /** 标准物流费 */
    private BigDecimal stdFreight;

    /** 免运费金额 */
    private BigDecimal stdFreeAmount;

    /** 新客物流费 */
    private BigDecimal newFreight;

    /** 新客免运费金额 */
    private BigDecimal newFreeAmount;

    /** 商户号 */
    private String merchantNo;

    /** 商户号名称 */
    private String merchantName;

    /** 区域经理列表 JSON */
    private String regionManagers;

    /** 覆盖城市列表 JSON */
    private String coveredCities;

    /** 创建人 */
    private String createdBy;

    /** 修改人 */
    private String updatedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 修改时间 */
    private LocalDateTime updatedAt;
}
