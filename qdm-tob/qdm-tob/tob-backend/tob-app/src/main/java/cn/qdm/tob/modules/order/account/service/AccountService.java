package cn.qdm.tob.modules.order.account.service;

import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.customer.cst.api.internal.CstArchiveApi;
import cn.qdm.tob.modules.customer.cst.api.internal.dto.CstArchiveDTO;
import cn.qdm.tob.modules.order.account.AccountMapping;
import cn.qdm.tob.modules.order.account.domain.OrdAccountTransaction;
import cn.qdm.tob.modules.order.account.domain.OrdCustomerAccount;
import cn.qdm.tob.modules.order.account.enums.AccountType;
import cn.qdm.tob.modules.order.account.enums.TransactionStatus;
import cn.qdm.tob.modules.order.account.enums.TransactionType;
import cn.qdm.tob.modules.order.flow.enums.PaymentMethod;
import cn.qdm.tob.modules.order.account.mapper.AccountTransactionMapper;
import cn.qdm.tob.modules.order.account.mapper.CustomerAccountMapper;
import cn.qdm.tob.modules.order.account.vo.AccountSummaryVO;
import cn.qdm.tob.modules.order.account.vo.CustomerInfoVO;
import cn.qdm.tob.modules.order.account.vo.TransactionCreateVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 客户账户管理服务
 *
 * <p>跨模块依赖：通过 {@link CstArchiveApi} 访问 customer 模块的客户档案数据，
 * 遵循 Modulith 跨模块边界规则（禁止直接引用 customer 模块的 Mapper）。</p>
 */
@Service
@RequiredArgsConstructor
public class AccountService extends TobBaseService<CustomerAccountMapper, OrdCustomerAccount> {

    private final CustomerAccountMapper customerAccountMapper;
    private final AccountTransactionMapper accountTransactionMapper;
    private final CstArchiveApi cstArchiveApi;
    private final AccountMapping mapping;

    // ================================================================
    // 分页查询
    // ================================================================

    /**
     * 分页查询客户账户列表（应用层两步组合查询）
     *
     * <p>客户名称、营业执照等字段不冗余，通过 CstArchiveApi 从 customer 模块实时获取。</p>
     */
    public Page<AccountSummaryVO> page(Integer pageNum, Integer pageSize,
                                        String keyword, String licenseNo, String accountType) {
        LambdaQueryWrapper<OrdCustomerAccount> wrapper = new LambdaQueryWrapper<>();

        // Step 1: 若有关键字或营业执照筛选，先通过 CstArchiveApi 查客户档案获取匹配的 customer_code
        List<String> matchedCodes = findMatchedCustomerCodes(keyword, licenseNo);
        if (matchedCodes != null) {
            if (matchedCodes.isEmpty()) {
                Page<AccountSummaryVO> emptyPage = new Page<>(pageNum, pageSize, 0);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
            wrapper.in(OrdCustomerAccount::getCustomerCode, matchedCodes);
        }

        // Step 2: 账户类型筛选
        if (accountType != null && !accountType.isBlank()) {
            wrapper.eq(OrdCustomerAccount::getAccountType, AccountType.valueOf(accountType));
        }

        wrapper.orderByDesc(OrdCustomerAccount::getUpdatedAt);

        // Step 3: 分页查询账户
        Page<OrdCustomerAccount> entityPage = customerAccountMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);

        // Step 4: 批量查询客户档案（通过 CstArchiveApi）
        List<OrdCustomerAccount> records = entityPage.getRecords();
        Map<String, CstArchiveDTO> archiveMap;
        if (!records.isEmpty()) {
            List<String> codes = records.stream()
                    .map(OrdCustomerAccount::getCustomerCode)
                    .distinct()
                    .collect(Collectors.toList());
            archiveMap = cstArchiveApi.getArchivesByCodes(codes)
                    .stream()
                    .collect(Collectors.toMap(CstArchiveDTO::getSapCustomerCode, a -> a, (a, b) -> a));
        } else {
            archiveMap = Collections.emptyMap();
        }

        // Step 5: 组装 VO
        List<AccountSummaryVO> voList = records.stream()
                .map(entity -> {
                    AccountSummaryVO vo = mapping.toSummary(entity);
                    CstArchiveDTO archive = archiveMap.get(entity.getCustomerCode());
                    if (archive != null) {
                        vo.setCustomerName(archive.getCompanyName());
                        vo.setLicenseNo(archive.getLicenseNo());
                        vo.setCompanyName(archive.getCompanyName());
                        vo.setLicensePhoto(archive.getLicensePhoto());
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        Page<AccountSummaryVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    // ================================================================
    // 查询客户信息
    // ================================================================

    /**
     * 按客户编码查询客户档案信息（通过 CstArchiveApi 跨模块调用）
     */
    public CustomerInfoVO getCustomerByCode(String code) {
        CstArchiveDTO archive = cstArchiveApi.getArchiveByCode(code);
        if (archive == null) {
            throw new RuntimeException("客户编码不存在，请确认后重新输入");
        }

        CustomerInfoVO vo = new CustomerInfoVO();
        vo.setCustomerCode(archive.getSapCustomerCode());
        vo.setCustomerName(archive.getCompanyName());
        vo.setLicenseNo(archive.getLicenseNo());
        vo.setCompanyName(archive.getCompanyName());
        vo.setLicensePhoto(archive.getLicensePhoto());
        return vo;
    }

    // ================================================================
    // 新增流水（充值/提现）
    // ================================================================

    /**
     * 新增充值/提现流水，自动更新账户余额
     */
    @Transactional(rollbackFor = Exception.class)
    public void createTransaction(TransactionCreateVO vo, Long operatorId, String operatorName) {
        // 1. 校验客户编码存在（通过 CstArchiveApi 跨模块调用）
        CstArchiveDTO archive = cstArchiveApi.getArchiveByCode(vo.getCustomerCode());
        if (archive == null) {
            throw new RuntimeException("客户编码不存在，请确认后重新输入");
        }

        // 2. 查找/创建账户
        OrdCustomerAccount account = customerAccountMapper.lambdaSelectOne(
                        w -> w.eq(OrdCustomerAccount::getCustomerCode, vo.getCustomerCode()))
                .orElseGet(() -> {
                    OrdCustomerAccount newAccount = new OrdCustomerAccount();
                    newAccount.setCustomerCode(vo.getCustomerCode());
                    newAccount.setAccountType(vo.getAccountType());
                    newAccount.setBalance(BigDecimal.ZERO);
                    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
                    newAccount.setCreatedAt(now);
                    newAccount.setUpdatedAt(now);
                    customerAccountMapper.insert(newAccount);
                    return newAccount;
                });

        // 3. 业务逻辑：PREPAID 余额实时变动，CREDIT 仅记录流水
        BigDecimal amount = vo.getAmount();
        BigDecimal balanceBefore = null;
        BigDecimal balanceAfter = null;

        if (vo.getAccountType() == AccountType.PREPAID) {
            if (vo.getTransactionType() == TransactionType.RECHARGE) {
                balanceBefore = account.getBalance();
                account.setBalance(account.getBalance().add(amount));
                balanceAfter = account.getBalance();
            } else if (vo.getTransactionType() == TransactionType.WITHDRAW) {
                AssertUtils.ge(account.getBalance(), amount,
                        "余额不足，当前余额：" + account.getBalance() + "元");
                balanceBefore = account.getBalance();
                account.setBalance(account.getBalance().subtract(amount));
                balanceAfter = account.getBalance();
            }
        }
        // CREDIT 类型：不扣减余额，balance_before/after 为 null（默认值）

        // 4. 生成流水号
        String transactionNo = generateTransactionNo();

        // 5. 保存流水记录
        OrdAccountTransaction tx = new OrdAccountTransaction();
        tx.setTransactionNo(transactionNo);
        tx.setAccountId(account.getId());
        tx.setAccountType(vo.getAccountType());
        tx.setTransactionType(vo.getTransactionType());
        tx.setAmount(amount);
        tx.setBalanceBefore(balanceBefore);
        tx.setBalanceAfter(balanceAfter);
        // 账户管理的"客户"是资金流水中的"结算账户"（充值/提现场景没有消费客户）
        tx.setSettlementAccountCode(vo.getCustomerCode());
        tx.setSettlementAccountName(archive.getCompanyName());
        tx.setPaymentMethod(PaymentMethod.PREPAID);
        tx.setOperatorId(operatorId);
        tx.setOperatorName(operatorName);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setCreatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        accountTransactionMapper.insert(tx);

        // 6. 更新账户（仅 PREPAID）
        if (vo.getAccountType() == AccountType.PREPAID) {
            account.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
            customerAccountMapper.updateById(account);
        }
    }

    // ================================================================
    // 私有方法
    // ================================================================

    /**
     * 根据关键字和营业执照编号从客户档案查找匹配的 customer_code
     *
     * @return null=无需过滤, 空列表=无匹配, 非空列表=需过滤的 code 集合
     */
    private List<String> findMatchedCustomerCodes(String keyword, String licenseNo) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasLicense = licenseNo != null && !licenseNo.isBlank();

        if (!hasKeyword && !hasLicense) {
            return null;
        }

        List<String> byKeyword = hasKeyword
                ? cstArchiveApi.searchByKeyword(keyword).stream()
                        .map(CstArchiveDTO::getSapCustomerCode)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                : null;

        List<String> byLicense = hasLicense
                ? cstArchiveApi.searchByLicenseNo(licenseNo).stream()
                        .map(CstArchiveDTO::getSapCustomerCode)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                : null;

        if (hasKeyword && hasLicense) {
            // 取交集
            byKeyword.retainAll(byLicense);
            return byKeyword;
        }
        return hasKeyword ? byKeyword : byLicense;
    }

    /**
     * 生成流水号：yyyyMMdd + 4位序号
     */
    private String generateTransactionNo() {
        String todayPrefix = LocalDate.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        List<OrdAccountTransaction> todayTxs = accountTransactionMapper.selectList(
                new LambdaQueryWrapper<OrdAccountTransaction>()
                        .likeRight(OrdAccountTransaction::getTransactionNo, todayPrefix)
                        .orderByDesc(OrdAccountTransaction::getTransactionNo)
                        .last("LIMIT 1"));

        int seq = 1;
        if (!todayTxs.isEmpty()) {
            String maxNo = todayTxs.get(0).getTransactionNo();
            if (maxNo.length() >= 12) {
                try {
                    seq = Integer.parseInt(maxNo.substring(8)) + 1;
                } catch (NumberFormatException e) {
                    // fallback to 1
                }
            }
        }

        return todayPrefix + String.format("%04d", seq);
    }
}
