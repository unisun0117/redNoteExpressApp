package cn.qdm.tob.modules.order.flow.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资金流水查询参数（11个筛选条件）
 */
@Data
public class AccountTransactionQuery {

    /** 入账时间起 */
    private LocalDateTime startTime;

    /** 入账时间止 */
    private LocalDateTime endTime;

    /** 收支类型：INCOME / EXPENSE */
    private String incomeExpenseType;

    /** 客户信息（编号/名称模糊） */
    private String customerKeyword;

    /** 结算账户（编号/名称模糊） */
    private String settlementKeyword;

    /** 付款方式 */
    private String paymentMethod;

    /** 账务类型 */
    private String transactionType;

    /** 第三方流水号 */
    private String thirdPartyFlowNo;

    /** 业务单号 */
    private String businessNo;

    /** 流水号 */
    private String flowNo;

    /** 订单号 */
    private String orderNo;

    /** 操作人（姓名/手机模糊） */
    private String operatorKeyword;
}
