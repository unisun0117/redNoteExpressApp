package cn.qdm.tob.modules.product.pricing.vo;

import cn.qdm.tob.framework.excel.annotation.ExcelColumn;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 价格组明细导出/导入 VO
 * <p>
 * 导入匹配键：销售大区 + 价格组编码 + 商品条码
 * 导入不触发价格变动审批
 */
@Data
public class PriceDetailExportVO {

    @ExcelColumn(title = "销售大区编号", index = 0)
    @NotBlank(message = "销售大区不能为空")
    private String salesRegionCode;

    @ExcelColumn(title = "价格组编码", index = 1)
    @NotBlank(message = "价格组编码不能为空")
    private String priceGroupCode;

    @ExcelColumn(title = "价格组名称", index = 2)
    private String priceGroupName;

    @ExcelColumn(title = "商品条码", index = 3)
    @NotBlank(message = "商品条码不能为空")
    private String productBarcode;

    /** 导入时由条码反查，可选 */
    @ExcelColumn(title = "商品名称", index = 4)
    private String productName;

    @ExcelColumn(title = "售价", index = 5)
    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    @ExcelColumn(title = "审批状态", index = 6)
    private String approvalStatus;

    @ExcelColumn(title = "更新人", index = 7)
    private String updatedBy;

    @ExcelColumn(title = "更新时间", index = 8)
    private String updatedAt;
}
