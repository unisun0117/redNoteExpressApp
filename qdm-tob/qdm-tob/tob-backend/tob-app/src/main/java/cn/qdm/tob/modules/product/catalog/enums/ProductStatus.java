package cn.qdm.tob.modules.product.catalog.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;

/**
 * 商品资料状态枚举
 */
@Getter
public enum ProductStatus implements Describable {

    /** 已上架 — 小程序端可见 */
    LISTED("已上架"),

    /** 已下架 — 小程序端不可见 */
    UNLISTED("已下架"),
    ;

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }
}
