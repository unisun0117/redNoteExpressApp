package cn.qdm.tob.modules.product.catalog.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.product.catalog.service.CategoryService;
import cn.qdm.tob.modules.product.catalog.dto.CategoryEditVO;
import cn.qdm.tob.modules.product.catalog.vo.CategoryViewVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品分类管理 API
 */
@Tag(name = "商品分类管理")
@RestController
@RequestMapping("/api/public/admin/product/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "分页查询商品分类列表")
    public ResponseResult<Page<CategoryViewVO>> list(
            @Parameter(description = "页码") @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "分类级别（0大分类/1中分类/2小分类）") @RequestParam(name = "level", required = false) Integer level,
            @Parameter(description = "分类编码模糊搜索") @RequestParam(name = "code", required = false) String code,
            @Parameter(description = "分类名称模糊搜索") @RequestParam(name = "name", required = false) String name) {
        return ResponseResult.success(categoryService.page(pageNum, pageSize, level, code, name));
    }

    @PutMapping
    @Operation(summary = "编辑商品分类自定义名称")
    public ResponseResult<?> updateAlias(
            @Parameter(description = "分类编号", required = true) @RequestParam(name = "id") String id,
            @Valid @RequestBody CategoryEditVO vo) {
        categoryService.updateAlias(id, vo);
        return ResponseResult.success();
    }
}
