package cn.qdm.tob.modules.order.flow.service;

import cn.qdm.tob.modules.order.account.domain.OrdAccountTransaction;
import cn.qdm.tob.modules.order.account.enums.TransactionType;
import cn.qdm.tob.modules.order.account.mapper.AccountTransactionMapper;
import cn.qdm.tob.modules.order.flow.AccountTransactionViewMapping;
import cn.qdm.tob.modules.order.flow.enums.PaymentMethod;
import cn.qdm.tob.modules.order.flow.vo.AccountTransactionViewVO;
import cn.qdm.tob.modules.order.flow.vo.TransactionSummaryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 资金流水查询服务（只读）
 */
@Service
@RequiredArgsConstructor
public class AccountTransactionQueryService {

    private final AccountTransactionMapper transactionMapper;
    private final AccountTransactionViewMapping mapping;

    // ================================================================
    // 分页列表
    // ================================================================

    public Page<AccountTransactionViewVO> page(
            Integer pageNum, Integer pageSize,
            LocalDateTime startTime, LocalDateTime endTime,
            String incomeExpenseType, String customerKeyword,
            String settlementKeyword, String paymentMethod, String transactionType,
            String thirdPartyFlowNo, String flowNo,
            String businessNo, String orderNo,
            String operatorKeyword) {

        LambdaQueryWrapper<OrdAccountTransaction> wrapper = buildQuery(
                startTime, endTime, incomeExpenseType, customerKeyword,
                settlementKeyword, paymentMethod, transactionType, thirdPartyFlowNo, flowNo,
                businessNo, orderNo, operatorKeyword);
        wrapper.orderByDesc(OrdAccountTransaction::getCreatedAt);

        Page<OrdAccountTransaction> entityPage = transactionMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);

        List<AccountTransactionViewVO> voList = entityPage.getRecords().isEmpty()
                ? Collections.emptyList()
                : fillIncomeExpenseType(mapping.toViewList(entityPage.getRecords()));

        Page<AccountTransactionViewVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    // ================================================================
    // 汇总统计
    // ================================================================

    public TransactionSummaryVO summary(
            LocalDateTime startTime, LocalDateTime endTime,
            String incomeExpenseType, String customerKeyword,
            String settlementKeyword, String paymentMethod, String transactionType,
            String thirdPartyFlowNo, String flowNo,
            String businessNo, String orderNo,
            String operatorKeyword) {

        LambdaQueryWrapper<OrdAccountTransaction> wrapper = buildQuery(
                startTime, endTime, incomeExpenseType, customerKeyword,
                settlementKeyword, paymentMethod, transactionType, thirdPartyFlowNo, flowNo,
                businessNo, orderNo, operatorKeyword);

        List<OrdAccountTransaction> list = transactionMapper.selectList(wrapper);

        BigDecimal incomeAmount = BigDecimal.ZERO;
        long incomeCount = 0;
        BigDecimal expenseAmount = BigDecimal.ZERO;
        long expenseCount = 0;

        for (OrdAccountTransaction tx : list) {
            if (tx.getAmount() == null) continue;
            boolean isIncome = tx.getTransactionType() == TransactionType.RECHARGE;
            if (isIncome) {
                incomeAmount = incomeAmount.add(tx.getAmount());
                incomeCount++;
            } else {
                expenseAmount = expenseAmount.add(tx.getAmount());
                expenseCount++;
            }
        }

        TransactionSummaryVO vo = new TransactionSummaryVO();
        vo.setIncomeAmount(incomeAmount);
        vo.setIncomeCount(incomeCount);
        vo.setExpenseAmount(expenseAmount);
        vo.setExpenseCount(expenseCount);
        return vo;
    }

    // ================================================================
    // 导出
    // ================================================================

    public List<AccountTransactionViewVO> export(
            LocalDateTime startTime, LocalDateTime endTime,
            String incomeExpenseType, String customerKeyword,
            String settlementKeyword, String paymentMethod, String transactionType,
            String thirdPartyFlowNo, String flowNo,
            String businessNo, String orderNo,
            String operatorKeyword) {

        LambdaQueryWrapper<OrdAccountTransaction> wrapper = buildQuery(
                startTime, endTime, incomeExpenseType, customerKeyword,
                settlementKeyword, paymentMethod, transactionType, thirdPartyFlowNo, flowNo,
                businessNo, orderNo, operatorKeyword);
        wrapper.orderByDesc(OrdAccountTransaction::getCreatedAt);

        List<OrdAccountTransaction> entities = transactionMapper.selectList(
                new Page<>(1, 10000), wrapper);
        return entities.isEmpty() ? Collections.emptyList()
                : fillIncomeExpenseType(mapping.toViewList(entities));
    }

    /** 根据账务类型推导收支类型 */
    private List<AccountTransactionViewVO> fillIncomeExpenseType(List<AccountTransactionViewVO> list) {
        for (AccountTransactionViewVO vo : list) {
            if (vo.getTransactionType() != null) {
                if ("RECHARGE".equals(vo.getTransactionType())) {
                    vo.setIncomeExpenseType("INCOME");
                } else {
                    vo.setIncomeExpenseType("EXPENSE");
                }
            }
        }
        return list;
    }

    // ================================================================
    // 条件构建
    // ================================================================

    private LambdaQueryWrapper<OrdAccountTransaction> buildQuery(
            LocalDateTime startTime, LocalDateTime endTime,
            String incomeExpenseType, String customerKeyword,
            String settlementKeyword, String paymentMethod, String transactionType,
            String thirdPartyFlowNo, String flowNo,
            String businessNo, String orderNo,
            String operatorKeyword) {

        LambdaQueryWrapper<OrdAccountTransaction> wrapper = new LambdaQueryWrapper<>();

        // 时间范围
        if (startTime != null) wrapper.ge(OrdAccountTransaction::getCreatedAt, startTime);
        if (endTime != null) wrapper.le(OrdAccountTransaction::getCreatedAt, endTime);

        // 收支类型 → 转换为账务类型过滤（INCOME=充值/退款/收款, EXPENSE=提现/下单/转账）
        if (incomeExpenseType != null && !incomeExpenseType.isBlank()) {
            if ("INCOME".equals(incomeExpenseType)) {
                wrapper.in(OrdAccountTransaction::getTransactionType,
                        TransactionType.RECHARGE);
            } else {
                wrapper.in(OrdAccountTransaction::getTransactionType,
                        TransactionType.WITHDRAW);
            }
        }

        // 客户（编号+名称联合模糊）
        if (customerKeyword != null && !customerKeyword.isBlank()) {
            wrapper.and(w -> w
                    .like(OrdAccountTransaction::getCustomerCode, customerKeyword)
                    .or()
                    .like(OrdAccountTransaction::getCustomerName, customerKeyword));
        }

        // 结算账户（编号+名称联合模糊）
        if (settlementKeyword != null && !settlementKeyword.isBlank()) {
            wrapper.and(w -> w
                    .like(OrdAccountTransaction::getSettlementAccountCode, settlementKeyword)
                    .or()
                    .like(OrdAccountTransaction::getSettlementAccountName, settlementKeyword));
        }

        // 付款方式
        if (paymentMethod != null && !paymentMethod.isBlank()) {
            wrapper.eq(OrdAccountTransaction::getPaymentMethod, PaymentMethod.valueOf(paymentMethod));
        }

        // 账务类型
        if (transactionType != null && !transactionType.isBlank()) {
            wrapper.eq(OrdAccountTransaction::getTransactionType, TransactionType.valueOf(transactionType));
        }

        // 精确匹配
        if (thirdPartyFlowNo != null && !thirdPartyFlowNo.isBlank()) {
            wrapper.eq(OrdAccountTransaction::getThirdPartyFlowNo, thirdPartyFlowNo);
        }
        if (flowNo != null && !flowNo.isBlank()) {
            wrapper.eq(OrdAccountTransaction::getTransactionNo, flowNo);
        }
        if (businessNo != null && !businessNo.isBlank()) {
            wrapper.eq(OrdAccountTransaction::getBusinessNo, businessNo);
        }
        if (orderNo != null && !orderNo.isBlank()) {
            wrapper.eq(OrdAccountTransaction::getOrderNo, orderNo);
        }

        // 操作人（模糊）
        if (operatorKeyword != null && !operatorKeyword.isBlank()) {
            wrapper.like(OrdAccountTransaction::getOperatorName, operatorKeyword);
        }

        return wrapper;
    }
}
