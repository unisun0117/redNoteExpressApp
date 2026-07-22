package cn.qdm.tob.framework.excel.model;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 导出元数据，对应 .NET SyncExportMetadata。
 * <p>
 * 缓存 Controller 方法的导出配置，避免每次请求都反射解析。
 * </p>
 */
@Data
@Builder
public class ExportMetadata {

    /** 导出列定义 */
    private List<ExcelColumnInfo> columns;

    /** 导出模板类型 */
    private Class<?> templateType;

    /** Controller 方法 */
    private Method methodInfo;

    /** Controller 类型 */
    private Class<?> controllerType;

    /** Controller 名称 */
    private String controllerName;

    /** Action 名称 */
    private String actionName;
}
