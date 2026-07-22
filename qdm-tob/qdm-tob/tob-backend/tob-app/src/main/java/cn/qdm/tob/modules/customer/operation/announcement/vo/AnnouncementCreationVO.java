package cn.qdm.tob.modules.customer.operation.announcement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增公告入参 VO
 */
@Data
@Schema(description = "新增公告")
public class AnnouncementCreationVO {

    @Schema(description = "销售大区编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "销售大区编号不能为空")
    private String regionCode;

    @Schema(description = "公告内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "公告内容不能为空")
    @Size(max = 500, message = "公告内容不能超过500字")
    private String content;

    @Schema(description = "启用状态", defaultValue = "true")
    private Boolean enabled = true;
}
