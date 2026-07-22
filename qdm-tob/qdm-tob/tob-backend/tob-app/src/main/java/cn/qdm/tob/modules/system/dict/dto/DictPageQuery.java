package cn.qdm.tob.modules.system.dict.dto;

import cn.qdm.tob.framework.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典分页查询参数")
public class DictPageQuery extends PageQuery {

    @Schema(description = "搜索关键词（模糊匹配编码/名称）")
    private String keyword;
}
