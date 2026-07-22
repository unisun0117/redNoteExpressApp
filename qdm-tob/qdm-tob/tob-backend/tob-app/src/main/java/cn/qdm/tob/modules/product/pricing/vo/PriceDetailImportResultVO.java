package cn.qdm.tob.modules.product.pricing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果 VO
 */
@Data
@Schema(description = "导入结果")
public class PriceDetailImportResultVO {

    @Schema(description = "新增数量")
    private int createdCount = 0;

    @Schema(description = "更新数量")
    private int updatedCount = 0;

    @Schema(description = "失败数量")
    private int failedCount = 0;

    @Schema(description = "失败详情列表")
    private List<ImportError> errors = new ArrayList<>();

    /**
     * 添加失败记录
     */
    public void addError(int row, String reason) {
        ImportError error = new ImportError();
        error.setRow(row);
        error.setReason(reason);
        this.errors.add(error);
        this.failedCount++;
    }

    @Data
    @Schema(description = "导入失败详情")
    public static class ImportError {

        @Schema(description = "Excel 行号")
        private int row;

        @Schema(description = "失败原因")
        private String reason;
    }
}
