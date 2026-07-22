package cn.qdm.tob.infrastructure.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实体基类 — 包含审计字段（创建/修改的时间和操作人信息）。
 * <p>
 * 配合 {@link MyBatisMetaObjectHandler} 实现自动填充，业务代码无需手动 set。
 * 实体类继承此类即可自动获得审计能力。
 */
@Data
public abstract class TobBaseEntity {

    public static final String CREATED_AT = "createdAt";
    public static final String CREATED_BY = "createdBy";
    public static final String UPDATED_AT = "updatedAt";
    public static final String UPDATED_BY = "updatedBy";

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 创建人姓名 */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 修改人姓名 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
}
