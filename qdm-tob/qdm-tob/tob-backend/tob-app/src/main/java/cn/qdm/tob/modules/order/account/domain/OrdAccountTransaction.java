package cn.qdm.tob.modules.order.account.domain;

import cn.qdm.tob.modules.order.account.enums.AccountType;
import cn.qdm.tob.modules.order.account.enums.TransactionStatus;
import cn.qdm.tob.modules.order.account.enums.TransactionType;
import cn.qdm.tob.modules.order.flow.enums.PaymentMethod;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金流水记录实体
 */
@Data
@TableName("ord_account_transaction")
public class OrdAccountTransaction {

    @TableId
    private Long id;

    /** 流水号（YYYYMMDD + 4位序号） */
    private String transactionNo;

    /** 关联账户ID */
    private Long accountId;

    /** 冗余账户类型 */
    private AccountType accountType;

    // ===== V12 新增：客户信息（冗余快照） =====

    /** 客户编号（下单必填，充值/提现为空） */
    private String customerCode;

    /** 客户名称（冗余快照） */
    private String customerName;

    // ===== V12 新增：结算账户信息（冗余快照） =====

    /** 结算账户编号 */
    private String settlementAccountCode;

    /** 结算账户名称（冗余快照） */
    private String settlementAccountName;

    // ===== V12 新增：付款方式 =====

    /** 付款方式：WECHAT / PREPAID / CREDIT / BANK_CARD */
    private PaymentMethod paymentMethod;

    /** 流水类型（已有字段，扩展语义） */
    private TransactionType transactionType;

    /** 金额（>0） */
    private BigDecimal amount;

    /** 交易前余额 */
    private BigDecimal balanceBefore;

    /** 交易后余额 */
    private BigDecimal balanceAfter;

    // ===== V12 新增：关联单据 =====

    /** 业务单号 */
    private String businessNo;

    /** 订单号（仅订单产生的流水） */
    private String orderNo;

    /** 微信商户号（仅微信支付时有值） */
    private String wechatMerchantNo;

    /** 第三方流水号（微信 transaction_id / 支付宝 trade_no） */
    private String thirdPartyFlowNo;

    /** 业务描述 */
    private String description;

    // ===== 已有字段 =====

    /** 交易状态 */
    private TransactionStatus status;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 备注 */
    private String remark;

    /** 创建时间（入账时间） */
    private LocalDateTime createdAt;
}
