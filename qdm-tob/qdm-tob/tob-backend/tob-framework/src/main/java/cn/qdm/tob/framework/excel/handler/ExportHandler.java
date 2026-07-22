package cn.qdm.tob.framework.excel.handler;

import cn.qdm.tob.framework.excel.annotation.Exportable;
import cn.qdm.tob.framework.excel.model.ExportMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.InputStream;

/**
 * Excel 导出处理器接口
 * <p>
 * ExportInterceptor 生成 Excel 数据后，回调此接口处理结果（写入 HTTP Response、上传 OSS 等）。
 * </p>
 */
@FunctionalInterface
public interface ExportHandler {

    /**
     * 处理生成的 Excel 流
     *
     * @param request    当前 HTTP 请求
     * @param response   当前 HTTP 响应（处理器负责写入最终响应）
     * @param excelStream Excel 文件输入流（可读）
     * @param attr       导出特性配置
     * @param metadata   导出元数据（含模板类型、列定义、Controller/Action 名称等）
     */
    void handle(HttpServletRequest request,
                HttpServletResponse response,
                InputStream excelStream,
                Exportable attr,
                ExportMetadata metadata) throws Exception;
}
