package cn.qdm.tob.modules.product.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 编辑商品分类自定义名称入参 VO
 */
@Data
@Schema(description = "编辑商品分类自定义名称")
public class CategoryEditVO {

    @Schema(description = "自定义名称，可为空")
    private String alias;
}
