package cn.qdm.tob.modules.product.catalog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果 VO
 */
@Data
@Schema(description = "导入结果")
public class ImportResultVO {

    @Schema(description = "新增条数")
    private int createdCount;

    @Schema(description = "更新条数")
    private int updatedCount;

    @Schema(description = "失败条数")
    private int failedCount;

    @Schema(description = "失败明细")
    private List<ImportError> errors = new ArrayList<>();

    @Data
    @Schema(description = "导入失败明细")
    public static class ImportError {
        @Schema(description = "行号")
        private int row;

        @Schema(description = "失败原因")
        private String reason;
    }

    public void addError(int row, String reason) {
        this.errors.add(new ImportError() {{
            setRow(row);
            setReason(reason);
        }});
        this.failedCount++;
    }
}
