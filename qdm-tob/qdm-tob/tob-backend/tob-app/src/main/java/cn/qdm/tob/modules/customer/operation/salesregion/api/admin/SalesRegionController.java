package cn.qdm.tob.modules.customer.operation.salesregion.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.dto.SalesRegionSimpleDTO;
import cn.qdm.tob.modules.customer.operation.salesregion.service.SalesRegionService;
import cn.qdm.tob.modules.customer.operation.salesregion.vo.SalesRegionCreationVO;
import cn.qdm.tob.modules.customer.operation.salesregion.vo.SalesRegionEditVO;
import cn.qdm.tob.modules.customer.operation.salesregion.vo.SalesRegionViewVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 销售大区管理 API
 */
@RestController
@RequestMapping("/api/admin/operation/sales-region")
@RequiredArgsConstructor
public class SalesRegionController {

    private final SalesRegionService salesRegionService;

    @GetMapping("/list")
    @Operation(summary = "分页查询销售大区列表")
    public ResponseResult<Page<SalesRegionViewVO>> list(
            @Parameter(description = "页码") @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "大区编号/名称模糊搜索") @RequestParam(name = "region", required = false) String region,
            @Parameter(description = "下单服务启用状态") @RequestParam(name = "serviceEnabled", required = false) Boolean serviceEnabled) {
        return ResponseResult.success(
            salesRegionService.page(pageNum, pageSize, region, serviceEnabled));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有销售大区（id+name，供下拉/弹窗使用）")
    public ResponseResult<List<SalesRegionSimpleDTO>> all() {
        return ResponseResult.success(salesRegionService.listAll());
    }

    @GetMapping("/detail")
    @Operation(summary = "查询大区详情")
    public ResponseResult<SalesRegionViewVO> get(
            @Parameter(description = "销售大区编号", required = true) @RequestParam(name = "code") String code) {
        return ResponseResult.success(salesRegionService.getByCode(code));
    }

    @PostMapping
    @Operation(summary = "新增大区")
    public ResponseResult<?> create(@Valid @RequestBody SalesRegionCreationVO vo) {
        salesRegionService.create(vo);
        return ResponseResult.success();
    }

    @PutMapping
    @Operation(summary = "更新大区")
    public ResponseResult<?> update(
            @Parameter(description = "销售大区编号", required = true) @RequestParam(name = "code") String code,
            @Valid @RequestBody SalesRegionEditVO vo) {
        salesRegionService.update(code, vo);
        return ResponseResult.success();
    }

    @DeleteMapping
    @Operation(summary = "删除大区")
    public ResponseResult<?> delete(
            @Parameter(description = "销售大区编号", required = true) @RequestParam(name = "code") String code) {
        salesRegionService.delete(code);
        return ResponseResult.success();
    }
}
