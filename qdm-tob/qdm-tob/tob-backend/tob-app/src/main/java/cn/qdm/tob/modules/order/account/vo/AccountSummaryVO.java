package cn.qdm.tob.modules.order.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户账户列表出参
 */
@Data
@Schema(description = "客户账户摘要")
public class AccountSummaryVO {

    /** 账户ID */
    @Schema(description = "账户ID")
    private Long id;

    /** 客户编号（来源 cst_company_archive） */
    @Schema(description = "客户编号")
    private String customerCode;

    /** 客户名称（来源 cst_company_archive） */
    @Schema(description = "客户名称")
    private String customerName;

    /** 账户金额 */
    @Schema(description = "账户金额（元）")
    private BigDecimal balance;

    /** 账户类型 */
    @Schema(description = "账户类型：PREPAID=预付款 CREDIT=账期")
    private String accountType;

    /** 账期天数 */
    @Schema(description = "账期天数（仅账期类型）")
    private Integer creditDays;

    /** 下一对账日期 */
    @Schema(description = "下一对账日期")
    private LocalDate nextReconciliationDate;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

    /** 营业执照编号（来源 cst_company_archive） */
    @Schema(description = "营业执照编号")
    private String licenseNo;

    /** 公司名称（来源 cst_company_archive） */
    @Schema(description = "公司名称")
    private String companyName;

    /** 营业执照照片URL（来源 cst_company_archive） */
    @Schema(description = "营业执照照片URL")
    private String licensePhoto;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
