package cn.qdm.tob.modules.product.catalog;

import cn.qdm.tob.modules.product.catalog.domain.ProductCatalog;
import cn.qdm.tob.modules.product.catalog.vo.ProductCatalogCreationVO;
import cn.qdm.tob.modules.product.catalog.vo.ProductCatalogEditVO;
import cn.qdm.tob.modules.product.catalog.vo.ProductCatalogExportVO;
import cn.qdm.tob.modules.product.catalog.vo.ProductCatalogViewVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 商品资料模块 MapStruct 对象映射器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductCatalogMapping {

    /** CreationVO → 实体 */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductCatalog toEntity(ProductCatalogCreationVO vo);

    /** EditVO → 实体 */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "salesRegionCode", ignore = true)
    @Mapping(target = "salesRegionName", ignore = true)
    @Mapping(target = "productBarcode", ignore = true)
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "dailyStock", ignore = true)
    @Mapping(target = "dailyAvailable", ignore = true)
    @Mapping(target = "dailySold", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    ProductCatalog toEntity(ProductCatalogEditVO vo);

    /** 实体 → ViewVO */
    ProductCatalogViewVO toView(ProductCatalog entity);

    /** 批量实体 → ViewVO */
    List<ProductCatalogViewVO> toViewList(List<ProductCatalog> entities);

    /** 实体 → ExportVO */
    ProductCatalogExportVO toExport(ProductCatalog entity);

    /** 批量实体 → ExportVO */
    List<ProductCatalogExportVO> toExportList(List<ProductCatalog> entities);
}
