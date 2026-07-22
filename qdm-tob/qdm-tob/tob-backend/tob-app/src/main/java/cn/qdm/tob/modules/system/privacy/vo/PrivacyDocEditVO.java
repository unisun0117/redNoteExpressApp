package cn.qdm.tob.modules.system.privacy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑隐私文档入参 VO
 * <p>不含 docType —— 文档类型创建后不可修改。</p>
 */
@Data
@Schema(description = "编辑隐私文档")
public class PrivacyDocEditVO {

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "版本号不能为空")
    @Size(max = 50, message = "版本号长度不能超过50")
    private String version;

    @Schema(description = "H5 展示链接", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "H5 链接不能为空")
    @Pattern(regexp = "^https?://.*", message = "H5 链接必须以 http:// 或 https:// 开头")
    @Size(max = 500, message = "H5 链接长度不能超过500")
    private String h5Url;

    @Schema(description = "备注")
    @Size(max = 200, message = "备注长度不能超过200")
    private String remark;

    @Schema(description = "富文本内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "富文本内容不能为空")
    private String richContent;

    @Schema(description = "附件文件链接（可选，若上传文件则由后端生成覆盖）")
    @Size(max = 500, message = "文件链接长度不能超过500")
    private String fileUrl;
}
