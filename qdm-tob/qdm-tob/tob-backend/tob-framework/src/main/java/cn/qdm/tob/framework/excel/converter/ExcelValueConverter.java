package cn.qdm.tob.framework.excel.converter;

import java.lang.reflect.Field;

/**
 * Excel 值转换器接口
 * <p>
 * 用于在导出/导入时对单元格值进行双向转换。
 * </p>
 *
 * <pre>{@code
 * // 导出：编码 → 显示文本
 * Object convert(Object value, Field field);
 *
 * // 导入：显示文本 → 编码
 * Object revert(Object value, Field field);
 * }</pre>
 */
public interface ExcelValueConverter {

    /**
     * 正向转换：原始值 → Excel 显示值（导出时使用）
     *
     * @param value 原始属性值
     * @param field 对应的实体属性（可为 null）
     * @return 转换后的显示值
     */
    Object convert(Object value, Field field);

    /**
     * 反向转换：Excel 显示值 → 原始值（导入时使用）
     * <p>
     * 默认实现直接返回原值，子类按需覆盖。
     * </p>
     *
     * @param value   Excel 单元格中的值
     * @param field   对应的实体属性（可为 null）
     * @return 转换后的原始值
     */
    default Object revert(Object value, Field field) {
        return value;
    }
}
