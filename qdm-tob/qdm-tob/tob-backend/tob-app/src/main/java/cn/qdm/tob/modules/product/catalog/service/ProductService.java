package cn.qdm.tob.modules.product.catalog.service;

import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.product.catalog.ProductMapping;
import cn.qdm.tob.modules.product.catalog.domain.PrdProduct;
import cn.qdm.tob.modules.product.catalog.mapper.PrdProductMapper;
import cn.qdm.tob.modules.product.catalog.vo.ProductViewVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 商品资料 CRUD 服务
 */
@Service
@RequiredArgsConstructor
public class ProductService extends TobBaseService<PrdProductMapper, PrdProduct> {

    private final ProductMapping mapping;

    /**
     * 分页查询商品资料列表
     */
    public Page<ProductViewVO> page(Integer pageNum, Integer pageSize,
                                    String barcode, String name,
                                    String categoryId, String status) {
        LambdaQueryWrapper<PrdProduct> wrapper = new LambdaQueryWrapper<>();
        if (barcode != null && !barcode.isBlank()) {
            wrapper.like(PrdProduct::getBarcode, barcode);
        }
        if (name != null && !name.isBlank()) {
            wrapper.like(PrdProduct::getName, name);
        }
        if (categoryId != null && !categoryId.isBlank()) {
            wrapper.eq(PrdProduct::getCategoryId, categoryId);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(PrdProduct::getStatus, status);
        }
        wrapper.orderByDesc(PrdProduct::getUpdatedAt);

        Page<PrdProduct> entityPage = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Page<ProductViewVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(mapping.toViewList(entityPage.getRecords()));
        return voPage;
    }
}
