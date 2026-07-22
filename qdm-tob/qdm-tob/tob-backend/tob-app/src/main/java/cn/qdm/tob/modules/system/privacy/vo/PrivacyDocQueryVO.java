package cn.qdm.tob.modules.system.privacy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 隐私文档分页查询入参 VO
 */
@Data
@Schema(description = "隐私文档查询条件")
public class PrivacyDocQueryVO {

    @Schema(description = "文档类型（枚举名，可选）")
    private String docType;

    @Schema(description = "版本号（模糊，可选）")
    private String version;

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", defaultValue = "20")
    private Integer pageSize = 20;
}
