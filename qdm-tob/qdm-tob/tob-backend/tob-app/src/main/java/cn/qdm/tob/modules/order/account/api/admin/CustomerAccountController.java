package cn.qdm.tob.modules.order.account.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.infrastructure.security.util.SecurityUtil;
import cn.qdm.tob.modules.order.account.service.AccountService;
import cn.qdm.tob.modules.order.account.vo.AccountSummaryVO;
import cn.qdm.tob.modules.order.account.vo.CustomerInfoVO;
import cn.qdm.tob.modules.order.account.vo.TransactionCreateVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 客户账户管理 API
 */
@RestController
@RequestMapping("/api/admin/order/account")
@RequiredArgsConstructor
public class CustomerAccountController {

    private final AccountService accountService;

    // ================================================================
    // 账户列表
    // ================================================================

    @GetMapping("/list")
    @Operation(summary = "分页查询客户账户列表")
    @RequirePermission(Permissions.ACCOUNT_VIEW)
    public ResponseResult<Page<AccountSummaryVO>> list(
            @Parameter(description = "页码") @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "客户编号/名称（模糊）") @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "营业执照编号（精确）") @RequestParam(name = "licenseNo", required = false) String licenseNo,
            @Parameter(description = "账户类型：PREPAID / CREDIT") @RequestParam(name = "accountType", required = false) String accountType) {
        return ResponseResult.success(
                accountService.page(pageNum, pageSize, keyword, licenseNo, accountType));
    }

    // ================================================================
    // 查询客户信息
    // ================================================================

    @GetMapping("/customer")
    @Operation(summary = "按客户编码查询客户档案信息")
    @RequirePermission(Permissions.ACCOUNT_VIEW)
    public ResponseResult<CustomerInfoVO> getCustomer(
            @Parameter(description = "客户编码", required = true) @RequestParam(name = "code") String code) {
        return ResponseResult.success(accountService.getCustomerByCode(code));
    }

    // ================================================================
    // 新增流水
    // ================================================================

    @PostMapping("/transaction")
    @Operation(summary = "新增充值/提现流水")
    @RequirePermission(Permissions.ACCOUNT_TRANSACTION_CREATE)
    public ResponseResult<?> createTransaction(@Valid @RequestBody TransactionCreateVO vo) {
        UserPrincipal principal = SecurityUtil.getCurrentUser();
        accountService.createTransaction(vo, principal.getUserId(), principal.getUsername());
        return ResponseResult.success();
    }
}
