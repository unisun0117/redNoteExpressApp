package cn.qdm.tob.modules.customer.operation.announcement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公告分页查询入参 VO
 */
@Data
@Schema(description = "公告查询条件")
public class AnnouncementQueryVO {

    @Schema(description = "销售大区编号（可选，精确匹配）")
    private String regionCode;

    @Schema(description = "启用状态（可选）")
    private Boolean enabled;

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", defaultValue = "20")
    private Integer pageSize = 20;
}
