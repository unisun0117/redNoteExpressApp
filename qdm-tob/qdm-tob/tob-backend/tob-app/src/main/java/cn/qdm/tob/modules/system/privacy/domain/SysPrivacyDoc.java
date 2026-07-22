package cn.qdm.tob.modules.system.privacy.domain;

import cn.qdm.tob.infrastructure.base.TobBaseEntity;
import cn.qdm.tob.modules.system.privacy.enums.DocStatus;
import cn.qdm.tob.modules.system.privacy.enums.DocType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 隐私文档实体（隐私政策 / 摘要 / 用户协议 等）
 * <p>枚举以 name() 英文字符串存储（doc_type / status）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_privacy_doc")
public class SysPrivacyDoc extends TobBaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文档类型 */
    private DocType docType;

    /** 版本号 */
    private String version;

    /** H5 展示链接 */
    private String h5Url;

    /** 附件文件链接（可选） */
    private String fileUrl;

    /** 备注 */
    private String remark;

    /** 富文本内容 */
    private String richContent;

    /** 状态 */
    private DocStatus status;
}
