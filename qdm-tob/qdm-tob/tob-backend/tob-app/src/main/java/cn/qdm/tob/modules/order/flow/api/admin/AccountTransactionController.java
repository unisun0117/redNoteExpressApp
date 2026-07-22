package cn.qdm.tob.modules.order.flow.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.modules.order.flow.service.AccountTransactionQueryService;
import cn.qdm.tob.modules.order.flow.vo.AccountTransactionViewVO;
import cn.qdm.tob.modules.order.flow.vo.TransactionSummaryVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资金流水查询 API
 */
@RestController
@RequestMapping("/api/admin/order/transaction")
@RequiredArgsConstructor
public class AccountTransactionController {

    private final AccountTransactionQueryService queryService;

    // ================================================================
    // 流水列表
    // ================================================================

    @GetMapping("/list")
    @Operation(summary = "分页查询资金流水")
    @RequirePermission(Permissions.TRANSACTION_VIEW)
    public ResponseResult<Page<AccountTransactionViewVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "入账时间起") @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "入账时间止") @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "收支类型：INCOME/EXPENSE") @RequestParam(required = false) String incomeExpenseType,
            @Parameter(description = "客户信息（编号/名称模糊）") @RequestParam(required = false) String customerKeyword,
            @Parameter(description = "结算账户（编号/名称模糊）") @RequestParam(required = false) String settlementKeyword,
            @Parameter(description = "付款方式") @RequestParam(required = false) String paymentMethod,
            @Parameter(description = "账务类型") @RequestParam(required = false) String transactionType,
            @Parameter(description = "第三方流水号") @RequestParam(required = false) String thirdPartyFlowNo,
            @Parameter(description = "业务单号") @RequestParam(required = false) String businessNo,
            @Parameter(description = "流水号") @RequestParam(required = false) String flowNo,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "操作人（姓名/手机模糊）") @RequestParam(required = false) String operatorKeyword) {
        return ResponseResult.success(queryService.page(
                pageNum, pageSize, startTime, endTime, incomeExpenseType,
                customerKeyword, settlementKeyword, paymentMethod, transactionType,
                thirdPartyFlowNo, flowNo, businessNo, orderNo, operatorKeyword));
    }

    // ================================================================
    // 汇总统计
    // ================================================================

    @GetMapping("/summary")
    @Operation(summary = "资金流水汇总统计（收入/支出金额及笔数）")
    @RequirePermission(Permissions.TRANSACTION_VIEW)
    public ResponseResult<TransactionSummaryVO> summary(
            @Parameter(description = "入账时间起") @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "入账时间止") @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "收支类型") @RequestParam(required = false) String incomeExpenseType,
            @Parameter(description = "客户信息") @RequestParam(required = false) String customerKeyword,
            @Parameter(description = "结算账户") @RequestParam(required = false) String settlementKeyword,
            @Parameter(description = "付款方式") @RequestParam(required = false) String paymentMethod,
            @Parameter(description = "账务类型") @RequestParam(required = false) String transactionType,
            @Parameter(description = "第三方流水号") @RequestParam(required = false) String thirdPartyFlowNo,
            @Parameter(description = "业务单号") @RequestParam(required = false) String businessNo,
            @Parameter(description = "流水号") @RequestParam(required = false) String flowNo,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "操作人") @RequestParam(required = false) String operatorKeyword) {
        return ResponseResult.success(queryService.summary(
                startTime, endTime, incomeExpenseType, customerKeyword,
                settlementKeyword, paymentMethod, transactionType, thirdPartyFlowNo,
                flowNo, businessNo, orderNo, operatorKeyword));
    }

    // ================================================================
    // 导出
    // ================================================================

    @GetMapping("/export")
    @Operation(summary = "导出资金流水（上限 10000 条）")
    @RequirePermission(Permissions.TRANSACTION_EXPORT)
    public ResponseResult<List<AccountTransactionViewVO>> exportList(
            @Parameter(description = "入账时间起") @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "入账时间止") @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "收支类型") @RequestParam(required = false) String incomeExpenseType,
            @Parameter(description = "客户信息") @RequestParam(required = false) String customerKeyword,
            @Parameter(description = "结算账户") @RequestParam(required = false) String settlementKeyword,
            @Parameter(description = "付款方式") @RequestParam(required = false) String paymentMethod,
            @Parameter(description = "账务类型") @RequestParam(required = false) String transactionType,
            @Parameter(description = "第三方流水号") @RequestParam(required = false) String thirdPartyFlowNo,
            @Parameter(description = "业务单号") @RequestParam(required = false) String businessNo,
            @Parameter(description = "流水号") @RequestParam(required = false) String flowNo,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "操作人") @RequestParam(required = false) String operatorKeyword) {
        return ResponseResult.success(queryService.export(
                startTime, endTime, incomeExpenseType, customerKeyword,
                settlementKeyword, paymentMethod, transactionType, thirdPartyFlowNo,
                flowNo, businessNo, orderNo, operatorKeyword));
    }
}
