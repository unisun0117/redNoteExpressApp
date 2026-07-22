package cn.qdm.tob.modules.product.catalog.vo;

import lombok.Data;

/**
 * 商品条码反查出参 VO
 */
@Data
public class SkuLookupVO {

    private String barcode;
    private String productName;
    private String spec;
    private String unit;
}
