package cn.qdm.tob.modules.product.catalog.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.product.catalog.api.internal.dto.ProductCatalogDTO;
import cn.qdm.tob.modules.product.catalog.domain.ProductCatalog;
import cn.qdm.tob.modules.product.catalog.vo.ProductMallVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品资料 Mapper
 */
@Mapper
public interface ProductCatalogMapper extends TobBaseMapper<ProductCatalog> {

    /**
     * 小程序商品列表（INNER JOIN 价格表，仅返回有售价的已上架商品）
     */
    Page<ProductMallVO> listForMall(@Param("page") Page<ProductMallVO> page,
                                     @Param("priceGroupCode") String priceGroupCode);

    /**
     * 按条码列表查询商品+价格
     */
    List<ProductCatalogDTO> listByBarcodesWithPrice(@Param("priceGroupCode") String priceGroupCode,
                                                    @Param("barcodes") List<String> barcodes);
}
