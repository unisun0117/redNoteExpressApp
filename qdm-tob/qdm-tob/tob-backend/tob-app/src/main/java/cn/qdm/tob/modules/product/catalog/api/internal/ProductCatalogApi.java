package cn.qdm.tob.modules.product.catalog.api.internal;

import cn.qdm.tob.modules.product.catalog.api.internal.dto.ProductCatalogDTO;
import cn.qdm.tob.modules.product.catalog.service.ProductCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品资料内部 API — 委托给 ProductCatalogService
 */
@Component
@RequiredArgsConstructor
public class ProductCatalogApi {

    private final ProductCatalogService productCatalogService;
    /**
     * 按条码列表查询商品+价格（INNER JOIN 价格表）
     */
    public List<ProductCatalogDTO> listByBarcodesWithPrice(String priceGroupCode,
                                                            List<String> barcodes) {
        return productCatalogService.listByBarcodesWithPrice(priceGroupCode, barcodes);
    }
}
