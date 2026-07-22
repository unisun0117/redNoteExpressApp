package cn.qdm.tob.modules.customer.operation.announcement.domain;

import cn.qdm.tob.infrastructure.base.TobBaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告实体
 * <p>
 * 继承 {@link TobBaseEntity} 获得审计字段（创建/修改的时间和操作人），由
 * MyBatisMetaObjectHandler 在 insert/update 时自动填充，业务代码无需手动 set。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_announcement")
public class OprAnnouncement extends TobBaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 销售大区编号 */
    private String regionCode;

    /** 销售大区名称（冗余，创建时按编号解析写入，避免跨表 JOIN） */
    private String regionName;

    /** 公告内容 */
    private String content;

    /** 启用状态 true=启用 false=停用 */
    private Boolean enabled;
}
