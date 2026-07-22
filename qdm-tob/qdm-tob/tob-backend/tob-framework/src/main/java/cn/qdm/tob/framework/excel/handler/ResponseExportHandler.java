package cn.qdm.tob.framework.excel.handler;

import cn.qdm.tob.framework.excel.annotation.Exportable;
import cn.qdm.tob.framework.excel.model.SheetFormat;
import cn.qdm.tob.framework.excel.model.ExportMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * HTTP Response 导出处理器
 * <p>
 * 将 Excel 流直接写入 HTTP Response，实现浏览器下载。
 * 禁用响应缓冲，实现边读边发的流式下载。
 * </p>
 */
public class ResponseExportHandler implements ExportHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       InputStream excelStream,
                       Exportable attr,
                       ExportMetadata metadata) throws Exception {

        String description = attr.name();
        if (StringUtils.isBlank(description)) {
            description = metadata.getActionName();
        }

        String fileName;
        String contentType;

        if (attr.format() == SheetFormat.CSV) {
            fileName = description + ".csv";
            contentType = "text/csv; charset=UTF-8";
        } else {
            fileName = description + ".xlsx";
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        response.setContentType(contentType);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        var contentDisposition = ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        // 禁用响应缓冲，实现边读边发的流式下载
        response.setBufferSize(0);

        try (excelStream) {
            excelStream.transferTo(response.getOutputStream());
            response.getOutputStream().flush();
        }
    }
}
