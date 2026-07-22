package cn.qdm.tob.modules.customer.operation.salesman.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.customer.operation.salesman.service.SalesmanService;
import cn.qdm.tob.modules.customer.operation.salesman.vo.AddSalesmanVO;
import cn.qdm.tob.modules.customer.operation.salesman.vo.SalesmanPerformanceVO;
import cn.qdm.tob.modules.customer.operation.salesman.vo.SalesmanReferralVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 业务员管理 API
 */
@RestController
@RequestMapping("/api/admin/salesman")
@RequiredArgsConstructor
public class SalesmanController {

    private final SalesmanService salesmanService;

    // ===== 推荐码管理 =====

    @GetMapping("/referral/list")
    @Operation(summary = "分页查询业务员推荐码列表")
    public ResponseResult<Page<SalesmanReferralVO>> listReferrals(
            @Parameter(description = "页码") @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "业务员姓名") @RequestParam(name = "name", required = false) String name,
            @Parameter(description = "手机号") @RequestParam(name = "phone", required = false) String phone) {
        return ResponseResult.success(
                salesmanService.pageReferrals(pageNum, pageSize, name, phone));
    }

    @PostMapping("/add")
    @Operation(summary = "添加业务员")
    public ResponseResult<SalesmanReferralVO> add(@Valid @RequestBody AddSalesmanVO vo) {
        return ResponseResult.success(salesmanService.add(vo));
    }

    @PostMapping("/referral/regenerate")
    @Operation(summary = "重新生成推荐码")
    public ResponseResult<?> regenerate(
            @Parameter(description = "业务员ID", required = true) @RequestParam(name = "id") Long id,
            @Parameter(description = "操作人") @RequestParam(name = "updatedBy", required = false) String updatedBy) {
        salesmanService.regenerateCode(id, updatedBy);
        return ResponseResult.success();
    }

    @PostMapping("/referral/clear")
    @Operation(summary = "置空推荐码")
    public ResponseResult<?> clear(
            @Parameter(description = "业务员ID", required = true) @RequestParam(name = "id") Long id,
            @Parameter(description = "操作人") @RequestParam(name = "updatedBy", required = false) String updatedBy) {
        salesmanService.clearCode(id, updatedBy);
        return ResponseResult.success();
    }

    @DeleteMapping("/referral")
    @Operation(summary = "删除推荐码（标记失效）")
    public ResponseResult<?> deleteReferral(
            @Parameter(description = "业务员ID", required = true) @RequestParam(name = "id") Long id,
            @Parameter(description = "操作人") @RequestParam(name = "updatedBy", required = false) String updatedBy) {
        salesmanService.deleteCode(id, updatedBy);
        return ResponseResult.success();
    }

    // ===== 全部业务员列表（下拉选项用） =====

    @GetMapping("/list-all")
    @Operation(summary = "获取全部业务员列表（下拉选项用）")
    public ResponseResult<List<SalesmanReferralVO>> listAll() {
        return ResponseResult.success(salesmanService.listAll());
    }

    // ===== 绩效管理 =====

    @GetMapping("/performance/list")
    @Operation(summary = "分页查询绩效列表")
    public ResponseResult<Page<SalesmanPerformanceVO>> listPerformances(
            @Parameter(description = "页码") @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "业务员ID") @RequestParam(name = "salesmanId", required = false) Long salesmanId,
            @Parameter(description = "月度") @RequestParam(name = "month", required = false) String month) {
        return ResponseResult.success(
                salesmanService.pagePerformances(pageNum, pageSize, salesmanId, month));
    }

    @PostMapping("/performance")
    @Operation(summary = "新增绩效")
    public ResponseResult<?> addPerformance(
            @Valid @RequestBody SalesmanPerformanceVO vo,
            @Parameter(description = "操作人") @RequestParam(name = "updatedBy", required = false) String updatedBy) {
        salesmanService.addPerformance(vo, updatedBy);
        return ResponseResult.success();
    }

    @PutMapping("/performance")
    @Operation(summary = "编辑绩效")
    public ResponseResult<?> editPerformance(
            @Valid @RequestBody SalesmanPerformanceVO vo,
            @Parameter(description = "操作人") @RequestParam(name = "updatedBy", required = false) String updatedBy) {
        salesmanService.editPerformance(vo, updatedBy);
        return ResponseResult.success();
    }
}
