package cn.qdm.tob.modules.product.catalog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类列表出参 VO
 */
@Data
@Schema(description = "商品分类")
public class CategoryViewVO {

    @Schema(description = "分类编号")
    private String id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "自定义名称")
    private String alias;

    @Schema(description = "上级分类编号")
    private String parentId;

    @Schema(description = "上级分类名称")
    private String parentName;

    @Schema(description = "分类级别（0大分类/1中分类/2小分类）")
    private Integer level;

    @Schema(description = "分类级别名称")
    private String levelName;

    @Schema(description = "上级分类编号路径")
    private String parentIdPath;

    @Schema(description = "上级分类名称路径")
    private String parentNamePath;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String memo;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
