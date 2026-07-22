package cn.qdm.tob.modules.system.privacy.vo;

import cn.qdm.tob.framework.description.Description;
import cn.qdm.tob.modules.system.privacy.enums.DocStatus;
import cn.qdm.tob.modules.system.privacy.enums.DocType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 隐私文档详情出参 VO（含富文本）
 */
@Data
@Schema(description = "隐私文档详情")
public class PrivacyDocViewVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "文档类型")
    @Description
    private DocType docType;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "H5 展示链接")
    private String h5Url;

    @Schema(description = "附件文件链接")
    private String fileUrl;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "富文本内容")
    private String richContent;

    @Schema(description = "状态")
    @Description
    private DocStatus status;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "修改人")
    private String updatedBy;

    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;
}
