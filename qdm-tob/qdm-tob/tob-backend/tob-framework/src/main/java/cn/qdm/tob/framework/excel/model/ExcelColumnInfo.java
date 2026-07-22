package cn.qdm.tob.framework.excel.model;

import cn.qdm.tob.framework.excel.converter.ExcelValueConverter;
import lombok.Builder;
import lombok.Data;

/**
 * Excel 列元数据，对应 .NET ExcelColumnInfo。
 */
@Data
@Builder
public class ExcelColumnInfo {

    /** 属性名 */
    private String name;

    /** 表头显示名称 */
    private String title;

    /** 列顺序（值越小越靠前） */
    private int index;

    /** 列宽（0 表示默认宽度） */
    private double width;

    /** 格式化字符串 */
    private String format;

    /** 值转换器类型 */
    private Class<? extends ExcelValueConverter> converterType;
}
