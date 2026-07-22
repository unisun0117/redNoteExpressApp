package cn.qdm.tob.framework.excel.annotation;

import cn.qdm.tob.framework.excel.handler.ResponseExportHandler;
import cn.qdm.tob.framework.excel.handler.ExportHandler;
import cn.qdm.tob.framework.excel.model.SheetFormat;

import java.lang.annotation.*;

/**
 * 同步导出 Excel 特性注解
 * <p>
 * 标记在 Controller 的 Action 方法上，配合请求头 {@code Action: export} 触发导出。
 * 仅支持一次性导出和流式导出（不支持分页导出）。
 * </p>
 *
 * <pre>{@code
 * @GetMapping("/orders")
 * @Exportable(name = "订单列表", templateType = OrderExportDTO.class)
 * public ResponseResult<List<OrderExportDTO>> listOrders(OrderQueryDTO query) {
 *     // 正常返回数据
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Exportable {

    /**
     * 导出任务名称（同时作为导出文件名）
     */
    String name() default "";

    /**
     * 导出模板类，如果为 {@code Object.class}（默认）则自动从方法返回值类型解析
     */
    Class<?> templateType() default Object.class;

    /**
     * 需要导出的数据在返回对象的哪个属性路径上（用 . 分隔）
     * <p>
     * 例如返回对象为 {@code ResponseResult<PageResult<OrderDTO>>}，实际列表在 {@code data.list} 中，
     * 则设置为 {@code "data.list"}。
     * </p>
     */
    String dataPath() default "data.list";

    /**
     * Excel 导出处理器类型，需实现 {@link ExportHandler}，默认写入 HTTP Response
     */
    Class<? extends ExportHandler> handlerType() default ResponseExportHandler.class;

    /**
     * 导出文件格式，默认 XLSX
     */
    SheetFormat format() default SheetFormat.XLSX;
}
