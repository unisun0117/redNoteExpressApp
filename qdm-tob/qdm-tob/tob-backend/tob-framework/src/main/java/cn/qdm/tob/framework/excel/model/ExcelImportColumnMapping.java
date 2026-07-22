package cn.qdm.tob.framework.excel.model;

import cn.qdm.tob.framework.excel.annotation.ExcelColumn;
import cn.qdm.tob.framework.excel.converter.ExcelValueConverter;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Field;

/**
 * 导入列映射（内部类），对应 .NET ExcelImportColumnMapping。
 * <p>
 * 将实体属性与 Excel 列建立映射关系，同时持有预创建的 converter 实例。
 * </p>
 */
@Data
@Builder
public class ExcelImportColumnMapping {

    /** 匹配到的 Excel 表头名称 */
    private String headerName;

    /** 匹配到的 Excel 列索引（如 "A"、"B" 等） */
    private Integer headerIndex;

    /** 实体属性名 */
    private String propertyName;

    /** 实体属性反射信息 */
    private Field field;

    /** 属性上的 @ExcelColumn 注解 */
    private ExcelColumn excelColumnAttr;

    /** 预创建的值转换器实例（避免访问已释放的 ApplicationContext） */
    private ExcelValueConverter converterInstance;
}
