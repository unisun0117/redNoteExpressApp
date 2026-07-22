package cn.qdm.tob.modules.order.flow.service;

import cn.qdm.tob.modules.order.account.domain.OrdAccountTransaction;
import cn.qdm.tob.modules.order.account.enums.AccountType;
import cn.qdm.tob.modules.order.account.enums.TransactionStatus;
import cn.qdm.tob.modules.order.account.enums.TransactionType;
import cn.qdm.tob.modules.order.account.mapper.AccountTransactionMapper;
import cn.qdm.tob.modules.order.flow.AccountTransactionViewMapping;
import cn.qdm.tob.modules.order.flow.enums.PaymentMethod;
import cn.qdm.tob.modules.order.flow.vo.AccountTransactionViewVO;
import cn.qdm.tob.modules.order.flow.vo.TransactionSummaryVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountTransactionQueryService — 资金流水查询单元测试")
class AccountTransactionQueryServiceTest {

    @Mock private AccountTransactionMapper transactionMapper;
    @Mock private AccountTransactionViewMapping mapping;
    private AccountTransactionQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new AccountTransactionQueryService(transactionMapper, mapping);
    }

    // TC-01
    @Test @DisplayName("分页查询 — 无筛选")
    void shouldReturnPageWithoutFilters() {
        OrdAccountTransaction tx = buildTx(1L, TransactionType.RECHARGE, new BigDecimal("500.00"));
        when(transactionMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<OrdAccountTransaction>(1, 20, 1).setRecords(List.of(tx)));
        when(mapping.toView(tx)).thenReturn(toViewVO(tx));
        Page<AccountTransactionViewVO> result = queryService.page(1, 20, null, null, null, null, null, null, null, null, null, null, null, null);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getIncomeExpenseType()).isEqualTo("INCOME");
    }

    // TC-02
    @Test @DisplayName("分页查询 — 按收支类型 INCOME")
    void shouldFilterByIncomeExpenseType() {
        OrdAccountTransaction tx = buildTx(1L, TransactionType.RECHARGE, new BigDecimal("100.00"));
        when(transactionMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<OrdAccountTransaction>(1, 20, 1).setRecords(List.of(tx)));
        when(mapping.toView(tx)).thenReturn(toViewVO(tx));
        Page<AccountTransactionViewVO> result = queryService.page(1, 20, null, null, "INCOME", null, null, null, null, null, null, null, null, null);
        assertThat(result.getRecords().get(0).getIncomeExpenseType()).isEqualTo("INCOME");
    }

    // TC-03
    @Test @DisplayName("分页查询 — 付款方式 WECHAT")
    void shouldFilterByPaymentMethod() {
        OrdAccountTransaction tx = buildTx(1L, TransactionType.RECHARGE, new BigDecimal("200.00"));
        tx.setPaymentMethod(PaymentMethod.WECHAT);
        when(transactionMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<OrdAccountTransaction>(1, 20, 1).setRecords(List.of(tx)));
        when(mapping.toView(tx)).thenReturn(toViewVO(tx));
        Page<AccountTransactionViewVO> result = queryService.page(1, 20, null, null, null, null, null, "WECHAT", null, null, null, null, null, null);
        assertThat(result.getRecords().get(0).getPaymentMethod()).isEqualTo("WECHAT");
    }

    // TC-04
    @Test @DisplayName("汇总统计")
    void shouldReturnSummary() {
        OrdAccountTransaction i1 = buildTx(1L, TransactionType.RECHARGE, new BigDecimal("500.00"));
        OrdAccountTransaction i2 = buildTx(2L, TransactionType.RECHARGE, new BigDecimal("300.00"));
        OrdAccountTransaction e1 = buildTx(3L, TransactionType.WITHDRAW, new BigDecimal("200.00"));
        when(transactionMapper.selectList(any())).thenReturn(List.of(i1, i2, e1));
        TransactionSummaryVO summary = queryService.summary(null, null, null, null, null, null, null, null, null, null, null, null);
        assertThat(summary.getIncomeAmount()).isEqualByComparingTo(new BigDecimal("800.00"));
        assertThat(summary.getIncomeCount()).isEqualTo(2);
        assertThat(summary.getExpenseAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(summary.getExpenseCount()).isEqualTo(1);
    }

    // TC-05
    @Test @DisplayName("汇总统计 — 仅收入筛选")
    void shouldReturnOnlyIncomeSummary() {
        OrdAccountTransaction tx = buildTx(1L, TransactionType.RECHARGE, new BigDecimal("100.00"));
        when(transactionMapper.selectList(any())).thenReturn(List.of(tx));
        TransactionSummaryVO summary = queryService.summary(null, null, "INCOME", null, null, null, null, null, null, null, null, null);
        assertThat(summary.getIncomeCount()).isEqualTo(1);
        assertThat(summary.getExpenseCount()).isEqualTo(0);
    }

    // TC-06
    @Test @DisplayName("导出")
    void shouldExportList() {
        OrdAccountTransaction tx = buildTx(1L, TransactionType.WITHDRAW, new BigDecimal("50.00"));
        when(transactionMapper.selectList(any(Page.class), any())).thenReturn(List.of(tx));
        when(mapping.toView(tx)).thenReturn(toViewVO(tx));
        List<AccountTransactionViewVO> result = queryService.export(null, null, null, null, null, null, null, null, null, null, null, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIncomeExpenseType()).isEqualTo("EXPENSE");
    }

    private OrdAccountTransaction buildTx(Long id, TransactionType txType, BigDecimal amount) {
        OrdAccountTransaction tx = new OrdAccountTransaction();
        tx.setId(id); tx.setTransactionNo("20260712000" + id);
        tx.setAccountId(1L); tx.setAccountType(AccountType.PREPAID);
        tx.setTransactionType(txType); tx.setAmount(amount);
        tx.setPaymentMethod(PaymentMethod.PREPAID);
        tx.setStatus(TransactionStatus.SUCCESS); tx.setOperatorId(1L);
        tx.setOperatorName("测试操作人"); tx.setCreatedAt(LocalDateTime.now());
        return tx;
    }

    private AccountTransactionViewVO toViewVO(OrdAccountTransaction entity) {
        AccountTransactionViewVO vo = new AccountTransactionViewVO();
        vo.setId(entity.getId()); vo.setTransactionNo(entity.getTransactionNo());
        vo.setTransactionType(entity.getTransactionType() != null ? entity.getTransactionType().name() : null);
        vo.setIncomeExpenseType(entity.getTransactionType() == TransactionType.RECHARGE ? "INCOME" : "EXPENSE");
        vo.setPaymentMethod(entity.getPaymentMethod() != null ? entity.getPaymentMethod().name() : null);
        vo.setAmount(entity.getAmount()); vo.setBalanceAfter(entity.getBalanceAfter());
        vo.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        vo.setOperatorName(entity.getOperatorName()); vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
