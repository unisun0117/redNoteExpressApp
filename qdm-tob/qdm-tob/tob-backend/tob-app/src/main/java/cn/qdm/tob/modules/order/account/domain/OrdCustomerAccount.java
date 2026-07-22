package cn.qdm.tob.modules.order.account.domain;

import cn.qdm.tob.modules.order.account.enums.AccountType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户账户实体
 *
 * <p>客户名称、营业执照等字段不冗余，查询时从 cst_company_archive 实时获取。</p>
 */
@Data
@TableName("ord_customer_account")
public class OrdCustomerAccount {

    @TableId
    private Long id;

    /** 客户编码（关联 cst_company_archive.sap_customer_code） */
    private String customerCode;

    /** 账户余额（元） */
    private BigDecimal balance;

    /** 账户类型 */
    private AccountType accountType;

    /** 账期天数（仅 CREDIT 类型） */
    private Integer creditDays;

    /** 下一对账日期 */
    private LocalDate nextReconciliationDate;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 修改时间 */
    private LocalDateTime updatedAt;
}
