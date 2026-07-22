package cn.qdm.tob.modules.product.catalog.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类实体
 */
@Data
@TableName("prd_category")
public class PrdCategory {

    @TableId
    private String id;

    /** 分类名称 */
    private String name;

    /** 自定义-别名 */
    private String alias;

    /** 上级分类编号(0表示顶级分类) */
    private String parentId;

    /** 上级分类编号路径 */
    private String parentIdPath;

    /** 上级分类名称路径 */
    private String parentNamePath;

    /** 层级(0大分类,1中分类,2小分类) */
    private Integer level;

    /** 排序号 */
    private Integer sort;

    /** 状态 */
    private String status;

    /** 备注 */
    private String memo;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
