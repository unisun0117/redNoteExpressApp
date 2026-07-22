package cn.qdm.tob.modules.customer.operation.announcement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告列表/详情出参 VO
 */
@Data
@Schema(description = "公告")
public class AnnouncementViewVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "销售大区编号")
    private String regionCode;

    @Schema(description = "销售大区名称")
    private String regionName;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "启用状态")
    private Boolean enabled;

    @Schema(description = "修改人")
    private String updatedBy;

    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;
}
