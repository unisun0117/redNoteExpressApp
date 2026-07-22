package cn.qdm.tob.modules.customer.operation.announcement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑公告入参 VO（销售大区不可修改，故不含 regionCode）
 */
@Data
@Schema(description = "编辑公告")
public class AnnouncementEditVO {

    @Schema(description = "公告内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "公告内容不能为空")
    @Size(max = 500, message = "公告内容不能超过500字")
    private String content;

    @Schema(description = "启用状态")
    private Boolean enabled;
}
