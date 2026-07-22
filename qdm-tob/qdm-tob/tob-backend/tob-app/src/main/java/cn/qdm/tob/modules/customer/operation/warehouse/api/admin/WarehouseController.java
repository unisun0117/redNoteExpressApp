package cn.qdm.tob.modules.customer.operation.warehouse.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.customer.operation.warehouse.service.WarehouseService;
import cn.qdm.tob.modules.customer.operation.warehouse.vo.WarehouseCreationVO;
import cn.qdm.tob.modules.customer.operation.warehouse.vo.WarehouseEditVO;
import cn.qdm.tob.modules.customer.operation.warehouse.vo.WarehouseViewVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping("/list")
    @Operation(summary = "分页查询仓库列表")
    public ResponseResult<Page<WarehouseViewVO>> list(
            @Parameter(description = "页码") @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "仓库编码/名称") @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "销售大区") @RequestParam(name = "region", required = false) String region) {
        return ResponseResult.success(warehouseService.page(pageNum, pageSize, keyword, region));
    }

    @PostMapping
    @Operation(summary = "新增仓库")
    public ResponseResult<?> create(@Valid @RequestBody WarehouseCreationVO vo) {
        warehouseService.create(vo);
        return ResponseResult.success();
    }

    @PutMapping
    @Operation(summary = "编辑仓库")
    public ResponseResult<?> update(
            @Parameter(description = "仓库编码", required = true) @RequestParam(name = "code") String code,
            @Valid @RequestBody WarehouseEditVO vo) {
        warehouseService.update(code, vo);
        return ResponseResult.success();
    }

}
