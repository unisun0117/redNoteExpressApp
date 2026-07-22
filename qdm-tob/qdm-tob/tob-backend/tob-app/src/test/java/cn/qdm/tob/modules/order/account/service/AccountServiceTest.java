package cn.qdm.tob.modules.order.account.service;

import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.modules.customer.cst.api.internal.CstArchiveApi;
import cn.qdm.tob.modules.customer.cst.api.internal.dto.CstArchiveDTO;
import cn.qdm.tob.modules.order.account.AccountMapping;
import cn.qdm.tob.modules.order.account.domain.OrdAccountTransaction;
import cn.qdm.tob.modules.order.account.domain.OrdCustomerAccount;
import cn.qdm.tob.modules.order.account.enums.AccountType;
import cn.qdm.tob.modules.order.account.enums.TransactionStatus;
import cn.qdm.tob.modules.order.account.enums.TransactionType;
import cn.qdm.tob.modules.order.account.mapper.AccountTransactionMapper;
import cn.qdm.tob.modules.order.account.mapper.CustomerAccountMapper;
import cn.qdm.tob.modules.order.account.vo.AccountSummaryVO;
import cn.qdm.tob.modules.order.account.vo.CustomerInfoVO;
import cn.qdm.tob.modules.order.account.vo.TransactionCreateVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService — 客户账户管理单元测试")
class AccountServiceTest {

    @Mock
    private CustomerAccountMapper customerAccountMapper;

    @Mock
    private AccountTransactionMapper accountTransactionMapper;

    @Mock
    private CstArchiveApi cstArchiveApi;

    @Mock
    private AccountMapping mapping;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(customerAccountMapper, accountTransactionMapper, cstArchiveApi, mapping);
        ReflectionTestUtils.setField(accountService, "baseMapper", customerAccountMapper);
    }

    // ================================================================
    // TC-01: 分页查询（无筛选条件）
    // ================================================================

    @Test
    @DisplayName("分页查询 — 无筛选条件，返回账户列表并组合客户信息")
    void shouldReturnPageWithoutFilters() {
        // given
        OrdCustomerAccount account = buildAccount(1L, "C001", AccountType.PREPAID, new BigDecimal("1000.00"));
        when(customerAccountMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<OrdCustomerAccount>(1, 20, 1).setRecords(List.of(account)));
        when(cstArchiveApi.getArchivesByCodes(List.of("C001")))
                .thenReturn(List.of(buildArchive("C001", "测试公司", "123456789012345678", "http://img.jpg")));
        when(mapping.toSummary(account)).thenReturn(toSummaryVO(account));

        // when
        Page<AccountSummaryVO> result = accountService.page(1, 20, null, null, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);

        AccountSummaryVO vo = result.getRecords().get(0);
        assertThat(vo.getCustomerCode()).isEqualTo("C001");
        assertThat(vo.getAccountType()).isEqualTo("PREPAID");
        assertThat(vo.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(vo.getCompanyName()).isEqualTo("测试公司");
        assertThat(vo.getLicenseNo()).isEqualTo("123456789012345678");
    }

    // ================================================================
    // TC-02: 分页查询（按关键字模糊搜索 — 先查客户表再过滤）
    // ================================================================

    @Test
    @DisplayName("分页查询 — 按关键字模糊搜索（客户编号/名称）")
    void shouldFilterByKeyword() {
        // given: 关键字匹配到客户 C001
        when(cstArchiveApi.searchByKeyword("测试"))
                .thenReturn(List.of(buildArchive("C001", "测试公司", null, null)));

        OrdCustomerAccount account = buildAccount(1L, "C001", AccountType.PREPAID, new BigDecimal("500.00"));
        when(customerAccountMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<OrdCustomerAccount>(1, 20, 1).setRecords(List.of(account)));
        when(cstArchiveApi.getArchivesByCodes(List.of("C001")))
                .thenReturn(List.of(buildArchive("C001", "测试公司", null, null)));
        when(mapping.toSummary(account)).thenReturn(toSummaryVO(account));

        // when
        Page<AccountSummaryVO> result = accountService.page(1, 20, "测试", null, null);

        // then
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getCustomerCode()).isEqualTo("C001");
    }

    // ================================================================
    // TC-03: 分页查询（按账户类型筛选）
    // ================================================================

    @Test
    @DisplayName("分页查询 — 按账户类型筛选（CREDIT）")
    void shouldFilterByAccountType() {
        // given
        OrdCustomerAccount account = buildAccount(1L, "C001", AccountType.CREDIT, BigDecimal.ZERO);
        account.setCreditDays(30);
        when(customerAccountMapper.selectPage(any(Page.class), any()))
                .thenReturn(new Page<OrdCustomerAccount>(1, 20, 1).setRecords(List.of(account)));
        when(cstArchiveApi.getArchivesByCodes(List.of("C001")))
                .thenReturn(List.of(buildArchive("C001", "测试公司", null, null)));
        when(mapping.toSummary(account)).thenReturn(toSummaryVO(account));

        // when
        Page<AccountSummaryVO> result = accountService.page(1, 20, null, null, "CREDIT");

        // then
        assertThat(result.getRecords()).hasSize(1);
        AccountSummaryVO vo = result.getRecords().get(0);
        assertThat(vo.getAccountType()).isEqualTo("CREDIT");
        assertThat(vo.getCreditDays()).isEqualTo(30);
    }

    // ================================================================
    // TC-04: 查询客户信息（编码存在 → 返回信息）
    // ================================================================

    @Test
    @DisplayName("查询客户信息 — 编码存在，返回客户档案数据")
    void shouldReturnCustomerInfoWhenCodeExists() {
        // given
        when(cstArchiveApi.getArchiveByCode("C001"))
                .thenReturn(buildArchive("C001", "钱鲜达有限公司", "123456789012345678", "http://img.jpg"));

        // when
        CustomerInfoVO vo = accountService.getCustomerByCode("C001");

        // then
        assertThat(vo).isNotNull();
        assertThat(vo.getCustomerCode()).isEqualTo("C001");
        assertThat(vo.getCustomerName()).isEqualTo("钱鲜达有限公司");
        assertThat(vo.getLicenseNo()).isEqualTo("123456789012345678");
        assertThat(vo.getCompanyName()).isEqualTo("钱鲜达有限公司");
        assertThat(vo.getLicensePhoto()).isEqualTo("http://img.jpg");
    }

    // ================================================================
    // TC-05: 查询客户信息（编码不存在 → 抛异常）
    // ================================================================

    @Test
    @DisplayName("查询客户信息 — 编码不存在，抛出业务异常")
    void shouldThrowWhenCustomerCodeNotFound() {
        // given
        when(cstArchiveApi.getArchiveByCode("NOTEXIST")).thenReturn(null);

        // when / then
        assertThatThrownBy(() -> accountService.getCustomerByCode("NOTEXIST"))
                .isInstanceOf(TobServiceException.class)
                .hasMessageContaining("客户编码不存在");
    }

    // ================================================================
    // TC-06: 新增充值流水（正常 → 余额增加）
    // ================================================================

    @Test
    @DisplayName("新增充值流水 — 余额增加，流水号自动生成")
    void shouldIncreaseBalanceOnRecharge() {
        // given
        when(cstArchiveApi.getArchiveByCode("C001"))
                .thenReturn(buildArchive("C001", "测试公司", null, null));

        OrdCustomerAccount account = buildAccount(1L, "C001", AccountType.PREPAID, new BigDecimal("1000.00"));
        when(customerAccountMapper.lambdaSelectOne(any())).thenReturn(Optional.of(account));
        when(accountTransactionMapper.insert(any(OrdAccountTransaction.class))).thenReturn(1);
        when(customerAccountMapper.updateById(any(OrdCustomerAccount.class))).thenReturn(1);

        TransactionCreateVO vo = buildTransactionVO("C001", AccountType.PREPAID, TransactionType.RECHARGE, new BigDecimal("500.00"));

        // when
        accountService.createTransaction(vo, 1L, "操作人");

        // then: 余额应增加
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        verify(accountTransactionMapper).insert(argThat((OrdAccountTransaction tx) ->
                tx.getTransactionType() == TransactionType.RECHARGE
                && tx.getAmount().compareTo(new BigDecimal("500.00")) == 0
                && tx.getBalanceBefore().compareTo(new BigDecimal("1000.00")) == 0
                && tx.getBalanceAfter().compareTo(new BigDecimal("1500.00")) == 0
                && tx.getStatus() == TransactionStatus.SUCCESS
                && tx.getTransactionNo() != null
                && !tx.getTransactionNo().isEmpty()
                && tx.getOperatorId() == 1L
                && "操作人".equals(tx.getOperatorName())
                && "C001".equals(tx.getSettlementAccountCode())
                && "测试公司".equals(tx.getSettlementAccountName())
        ));
    }

    // ================================================================
    // TC-07: 新增提现流水（正常 → 余额减少）
    // ================================================================

    @Test
    @DisplayName("新增提现流水 — 余额减少")
    void shouldDecreaseBalanceOnWithdraw() {
        // given
        when(cstArchiveApi.getArchiveByCode("C001"))
                .thenReturn(buildArchive("C001", "测试公司", null, null));

        OrdCustomerAccount account = buildAccount(1L, "C001", AccountType.PREPAID, new BigDecimal("1000.00"));
        when(customerAccountMapper.lambdaSelectOne(any())).thenReturn(Optional.of(account));
        when(accountTransactionMapper.insert(any(OrdAccountTransaction.class))).thenReturn(1);
        when(customerAccountMapper.updateById(any(OrdCustomerAccount.class))).thenReturn(1);

        TransactionCreateVO vo = buildTransactionVO("C001", AccountType.PREPAID, TransactionType.WITHDRAW, new BigDecimal("300.00"));

        // when
        accountService.createTransaction(vo, 1L, "操作人");

        // then: 余额应减少
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
        verify(accountTransactionMapper).insert(argThat((OrdAccountTransaction tx) ->
                tx.getTransactionType() == TransactionType.WITHDRAW
                && tx.getBalanceBefore().compareTo(new BigDecimal("1000.00")) == 0
                && tx.getBalanceAfter().compareTo(new BigDecimal("700.00")) == 0
        ));
    }

    // ================================================================
    // TC-08: 新增提现流水（余额不足 → 抛异常）
    // ================================================================

    @Test
    @DisplayName("新增提现流水 — 余额不足，抛出业务异常")
    void shouldThrowWhenBalanceInsufficient() {
        // given
        when(cstArchiveApi.getArchiveByCode("C001"))
                .thenReturn(buildArchive("C001", "测试公司", null, null));

        OrdCustomerAccount account = buildAccount(1L, "C001", AccountType.PREPAID, new BigDecimal("100.00"));
        when(customerAccountMapper.lambdaSelectOne(any())).thenReturn(Optional.of(account));

        TransactionCreateVO vo = buildTransactionVO("C001", AccountType.PREPAID, TransactionType.WITHDRAW, new BigDecimal("500.00"));

        // when / then
        assertThatThrownBy(() -> accountService.createTransaction(vo, 1L, "操作人"))
                .isInstanceOf(TobServiceException.class)
                .hasMessageContaining("余额不足");
        verify(accountTransactionMapper, never()).insert(Collections.singleton(any()));
    }

    // ================================================================
    // TC-09: 新增流水（客户编码不存在 → 抛异常）
    // ================================================================

    @Test
    @DisplayName("新增流水 — 客户编码不存在，抛出业务异常")
    void shouldThrowWhenCustomerNotFoundOnTransaction() {
        // given
        when(cstArchiveApi.getArchiveByCode("NOTEXIST")).thenReturn(null);

        TransactionCreateVO vo = buildTransactionVO("NOTEXIST", AccountType.PREPAID, TransactionType.RECHARGE, new BigDecimal("100.00"));

        // when / then
        assertThatThrownBy(() -> accountService.createTransaction(vo, 1L, "操作人"))
                .isInstanceOf(TobServiceException.class)
                .hasMessageContaining("客户编码不存在");
        verify(accountTransactionMapper, never()).insert(Collections.singleton(any()));
    }

    // ================================================================
    // TC-10: 新增流水（账期类型 → 不扣减余额）
    // ================================================================

    @Test
    @DisplayName("新增流水 — 账期类型不实时扣减余额")
    void shouldNotChangeBalanceOnCreditTransaction() {
        // given
        when(cstArchiveApi.getArchiveByCode("C001"))
                .thenReturn(buildArchive("C001", "测试公司", null, null));

        OrdCustomerAccount account = buildAccount(1L, "C001", AccountType.CREDIT, BigDecimal.ZERO);
        when(customerAccountMapper.lambdaSelectOne(any())).thenReturn(Optional.of(account)) ;
        when(accountTransactionMapper.insert(any(OrdAccountTransaction.class))).thenReturn(1);

        TransactionCreateVO vo = buildTransactionVO("C001", AccountType.CREDIT, TransactionType.WITHDRAW, new BigDecimal("500.00"));

        // when
        accountService.createTransaction(vo, 1L, "操作人");

        // then: 账期类型余额不变
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(accountTransactionMapper).insert(argThat((OrdAccountTransaction tx) ->
                tx.getBalanceBefore() == null && tx.getBalanceAfter() == null
        ));
    }

    // ================================================================
    // 辅助方法
    // ================================================================

    private OrdCustomerAccount buildAccount(Long id, String customerCode, AccountType type, BigDecimal balance) {
        OrdCustomerAccount account = new OrdCustomerAccount();
        account.setId(id);
        account.setCustomerCode(customerCode);
        account.setAccountType(type);
        account.setBalance(balance);
        account.setCreditDays(type == AccountType.CREDIT ? 30 : null);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        return account;
    }

    private CstArchiveDTO buildArchive(String sapCustomerCode, String companyName, String licenseNo, String licensePhoto) {
        CstArchiveDTO dto = new CstArchiveDTO();
        dto.setId(1L);
        dto.setSapCustomerCode(sapCustomerCode);
        dto.setCompanyName(companyName);
        dto.setLicenseNo(licenseNo);
        dto.setLicensePhoto(licensePhoto);
        return dto;
    }

    private TransactionCreateVO buildTransactionVO(String customerCode, AccountType accountType,
                                                    TransactionType transactionType, BigDecimal amount) {
        TransactionCreateVO vo = new TransactionCreateVO();
        vo.setCustomerCode(customerCode);
        vo.setAccountType(accountType);
        vo.setTransactionType(transactionType);
        vo.setAmount(amount);
        return vo;
    }

    /** 模拟 MapStruct 转换：Entity → VO（不包含客户字段） */
    private AccountSummaryVO toSummaryVO(OrdCustomerAccount entity) {
        AccountSummaryVO vo = new AccountSummaryVO();
        vo.setId(entity.getId());
        vo.setCustomerCode(entity.getCustomerCode());
        vo.setBalance(entity.getBalance());
        vo.setAccountType(entity.getAccountType() != null ? entity.getAccountType().name() : null);
        vo.setCreditDays(entity.getCreditDays());
        vo.setNextReconciliationDate(entity.getNextReconciliationDate());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
