package cn.qdm.tob.framework.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel 导入特性注解
 * <p>
 * 标记在 Controller 的 Action 方法上，表示该方法支持自动解析上传的 Excel/CSV 文件。
 * 被拦截的方法参数可以是 {@code List<T>}（一次性读取）或 {@code Iterator<T>}（流式读取）。
 * </p>
 *
 * <pre>{@code
 * @PostMapping("/import")
 * @Importable(sheetName = "Sheet1", enableValidation = true)
 * public ResponseResult<?> importOrders(List<OrderImportDTO> list) {
 *     // list 已自动填充解析好的数据
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Importable {

    /**
     * multipart/form-data 中文件字段名称，默认 "file"
     */
    String fileFieldName() default "file";

    /**
     * 要读取的工作表名称，默认 null（第一个 Sheet）
     */
    String sheetName() default "";

    /**
     * 表头行索引（0-based），默认 0
     */
    int headerRowIndex() default 0;

    /**
     * 实现 {@link ImportHandler} 的处理器类型，默认 Void.class 表示不使用
     */
    Class<?> handlerType() default Void.class;

    /**
     * 是否启用 Jakarta Bean Validation 验证，默认 false
     */
    boolean enableValidation() default false;
}
