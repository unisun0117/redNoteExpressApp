package cn.qdm.tob.modules.product.catalog.vo;

import cn.qdm.tob.framework.excel.annotation.ExcelColumn;
import cn.qdm.tob.modules.product.catalog.enums.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品资料导出/导入 VO（排除图片和富文本字段）
 * <p>
 * 同时作为 {@code @Importable} 的模板类型，带 Jakarta Bean Validation 校验。
 */
@Data
public class ProductCatalogExportVO {

    @ExcelColumn(title = "销售大区编号", index = 0)
    @NotBlank(message = "销售大区编号不能为空")
    private String salesRegionCode;

    @ExcelColumn(title = "销售大区名称", index = 1)
    private String salesRegionName;

    @ExcelColumn(title = "商品条码", index = 2)
    @NotBlank(message = "商品条码不能为空")
    private String productBarcode;

    /** 导入时由条码反查，不可手动填写 */
    @ExcelColumn(title = "商品名称", index = 3)
    private String productName;

    @ExcelColumn(title = "仓库编码", index = 4)
    @NotBlank(message = "仓库编码不能为空")
    private String warehouseCode;

    @ExcelColumn(title = "仓库名称", index = 5)
    private String warehouseName;

    @ExcelColumn(title = "状态", index = 6)
    private ProductStatus status;

    @ExcelColumn(title = "小程序名称", index = 7)
    private String miniappName;

    @ExcelColumn(title = "订购基数", index = 8)
    @NotNull(message = "订购基数不能为空")
    @DecimalMin(value = "0", message = "订购基数不能小于0")
    private BigDecimal orderBaseQty;

    @ExcelColumn(title = "订购下限", index = 9)
    @NotNull(message = "订购下限不能为空")
    @DecimalMin(value = "0", message = "订购下限不能小于0")
    private BigDecimal orderMinQty;

    @ExcelColumn(title = "订购上限", index = 10)
    @NotNull(message = "订购上限不能为空")
    @DecimalMin(value = "0", message = "订购上限不能小于0")
    private BigDecimal orderMaxQty;

    @ExcelColumn(title = "每日可用库存", index = 11)
    @NotNull(message = "每日可用库存不能为空")
    @DecimalMin(value = "0", message = "每日可用库存不能小于0")
    private BigDecimal dailyStock;

    /** 导入时忽略，系统自动计算 */
    @ExcelColumn(title = "今日可用数量", index = 12)
    private BigDecimal dailyAvailable;

    /** 导入时忽略，由订单侧同步 */
    @ExcelColumn(title = "今日已售数量", index = 13)
    private BigDecimal dailySold;
}
