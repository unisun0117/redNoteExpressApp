package cn.qdm.tob.modules.system.sequence.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通用序列号配置实体，对应表 sys_sequence。
 * <p>每种序列以 seq_key 唯一标识，current_val 记录当前值，step 控制步长，formatter 控制输出格式。</p>
 * <p>注意：此表为配置表，不继承 TobBaseEntity，不需要审计字段。</p>
 */
@Data
@TableName("sys_sequence")
public class SysSequence {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 序列唯一标识，如 "USER_RECOMMEND_CODE"、"ORDER_NO" */
    private String seqKey;

    /** 当前序列值（已分配到的数值） */
    private Long currentVal;

    /** 自增步长，默认 1 */
    private Integer step;

    /** 格式化模板，如 "%04d" 输出 "0001"，不填则输出原始数字字符串 */
    private String formatter;

    /** 序列用途描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
