package cn.qdm.tob.modules.system.dict.domain;

import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.infrastructure.base.TobBaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
public class SysDict extends TobBaseEntity {
    /** 字典编码 */
    @TableId
    private String code;

    /** 字典名称 */
    private String name;

    private String description;

    private RecordStatus status;
}
