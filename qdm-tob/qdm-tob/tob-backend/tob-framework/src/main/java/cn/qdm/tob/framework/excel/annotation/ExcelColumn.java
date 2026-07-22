package cn.qdm.tob.framework.excel.annotation;

import cn.qdm.tob.framework.excel.converter.ExcelValueConverter;

import java.lang.annotation.*;

/**
 * Excel 列映射注解
 * <p>
 * 标注在实体类的属性上，定义该属性与 Excel 列的映射关系。
 * </p>
 *
 * <pre>{@code
 * @ExcelColumn(title = {"姓名"}, index = 0)
 * private String name;
 *
 * @ExcelColumn(title = {"状态"}, converterType = StatusConverter.class)
 * private Integer status;
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelColumn {

    /** 默认单列宽度 */
    double DEFAULT_WIDTH = 0d;

    /**
     * 表头名称，支持多个别名匹配（导入时任意一个匹配即可）
     */
    String[] title() default {};

    /**
     * 列顺序（导出时生效，值越小越靠前）
     */
    int index() default Integer.MAX_VALUE;

    /**
     * 列宽（导出时生效），0 表示使用默认宽度
     */
    double width() default DEFAULT_WIDTH;

    /**
     * 格式化字符串，如 "yyyy-MM-dd"、"0.00"
     */
    String format() default "";

    /**
     * 值转换器类型，需实现 {@link ExcelValueConverter}
     * <p>
     * 导出时调用 {@link ExcelValueConverter#convert}，
     * 导入时调用 {@link ExcelValueConverter#revert}。
     * </p>
     */
    Class<? extends ExcelValueConverter> converterType() default ExcelValueConverter.class;
}
