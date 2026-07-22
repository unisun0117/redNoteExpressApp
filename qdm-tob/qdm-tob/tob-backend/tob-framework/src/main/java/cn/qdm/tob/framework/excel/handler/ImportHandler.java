package cn.qdm.tob.framework.excel.handler;

import cn.qdm.tob.framework.excel.model.ExcelImportColumnMapping;

import java.util.List;
import java.util.Map;

/**
 * Excel 导入行处理器接口
 * <p>
 * 在导入过程的各个生命周期节点回调，可用于数据校验、日志记录、自定义处理等。
 * </p>
 *
 * @param <T> 导入的实体类型
 */
public interface ImportHandler<T> {

    /**
     * 解析表头之前回调
     *
     * @param sheetName 工作表名称
     * @param mappings  列映射列表（只读）
     */
    default void onBeforeParse(String sheetName, List<ExcelImportColumnMapping> mappings) {
    }

    /**
     * 每读取一行数据后回调
     *
     * @param entity  解析后的实体对象
     * @param rowIndex 行索引（0-based，不含表头）
     * @param cells   单元格原始值，key = 属性名
     */
    default void onRowRead(T entity, int rowIndex, Map<String, Object> cells) {
    }

    /**
     * 全部解析完成后回调
     *
     * @param entities 解析后的实体列表
     * @param count    总行数
     */
    default void onAfterParse(List<T> entities, int count) {
    }
}
