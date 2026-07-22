package cn.qdm.tob.framework.description;

import java.lang.annotation.*;

/**
 * 描述注解 —— 标记需要编码→显示文本转换的字段。
 * <p>
 * 用于两套机制：
 * <ul>
 *   <li><b>Jackson 序列化</b>：通过 {@code DescriptionJacksonModule} 自动追加 {@code _Name} 影子字段或替换原值</li>
 *   <li><b>Excel 导入/导出</b>：通过 {@code DictionaryConverter} 实现编码↔显示文本的双向转换</li>
 * </ul>
 * </p>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Description {
    /** 描述查找键 —— 由 {@link DescriptionProvider} 解析（如字典编码、静态规则等） */
    String value() default "";

    DescriptionStrategy strategy() default DescriptionStrategy.EXTENSION;

    String separator() default "";
}
