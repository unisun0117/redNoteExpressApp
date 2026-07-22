package cn.qdm.tob.modules.product.catalog.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.product.catalog.service.ProductService;
import cn.qdm.tob.modules.product.catalog.vo.ProductViewVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品资料管理 API（公开接口）
 */
@Tag(name = "商品资料管理")
@RestController
@RequestMapping("/api/public/admin/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")
    @Operation(summary = "分页查询商品资料列表")
    public ResponseResult<Page<ProductViewVO>> list(
            @Parameter(description = "页码") @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "商品条码（模糊）") @RequestParam(name = "barcode", required = false) String barcode,
            @Parameter(description = "商品名称（模糊）") @RequestParam(name = "name", required = false) String name,
            @Parameter(description = "小类编号") @RequestParam(name = "categoryId", required = false) String categoryId,
            @Parameter(description = "状态") @RequestParam(name = "status", required = false) String status) {
        return ResponseResult.success(
                productService.page(pageNum, pageSize, barcode, name, categoryId, status));
    }
}
