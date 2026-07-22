package cn.qdm.tob.modules.system.dict.domain;

import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.infrastructure.base.TobBaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典项实体
 * <p>
 * 复合主键 (dict_code, value)，MyBatis-Plus 不支持复合主键。
 * 禁止使用 getById / updateById / deleteById，请通过 LambdaQueryWrapper/LambdaUpdateWrapper 操作。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_item")
public class SysDictItem extends TobBaseEntity {
    /** 字典编码（主键之一） */
    private String dictCode;

    /** 数据值（主键之一）  */
    private String value;

    /** 显示文本 */
    private String label;

    /** 排序号 */
    private Integer sort;

    private RecordStatus status;
}
