package cn.qdm.tob.modules.product.catalog.api.mall;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.annotation.CurrentCustomer;
import cn.qdm.tob.infrastructure.security.resolver.CustomerContext;
import cn.qdm.tob.modules.product.catalog.service.CategoryService;
import cn.qdm.tob.modules.product.catalog.service.ProductCatalogService;
import cn.qdm.tob.modules.product.catalog.vo.CategoryViewVO;
import cn.qdm.tob.modules.product.catalog.vo.ProductMallVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端 — 商品/分类只读 API
 */
@RestController
@RequestMapping("/api/mall")
@RequiredArgsConstructor
public class ProductMallController {

    private final ProductCatalogService productCatalogService;
    private final CategoryService categoryService;

    @GetMapping("/products")
    @Operation(summary = "商品列表（仅已上架）")
    public ResponseResult<Page<ProductMallVO>> listProducts(
            @CurrentCustomer CustomerContext customer,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseResult.success(productCatalogService.listForMall(customer.getPriceGroup(), page, size));
    }

    @GetMapping("/categories")
    @Operation(summary = "商品分类列表（仅顶级分类）")
    public ResponseResult<List<CategoryViewVO>> listCategories() {
        return ResponseResult.success(categoryService.listTopLevel());
    }
}
