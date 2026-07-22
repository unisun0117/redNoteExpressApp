package cn.qdm.tob.infrastructure.web;

import cn.qdm.tob.framework.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 请求追踪 ID 过滤器
 * <p>
 * 为每个 HTTP 请求生成唯一 traceId，通过以下方式传递：
 * <ul>
 *   <li>写入 MDC，日志中通过 {@code %X{traceId}} 获取</li>
 *   <li>写入响应头 {@code X-Trace-Id}，便于前端排查</li>
 * </ul>
 * {@link GlobalExceptionHandler} 会从 MDC 读取 traceId
 * 填入 {@link cn.qdm.tob.framework.model.ResponseResult#traceId}。
 * </p>
 *
 * @author qdm-tob
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {
    private static final String REQUEST_ID_HEADER = "requestId";
    private static final String X_TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String traceId = resolveTraceId(request);
            MDC.put(Constants.TRACE_ID_KEY, traceId);
            response.setHeader(X_TRACE_ID_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(Constants.TRACE_ID_KEY);
        }
    }

    /**
     * 解析请求追踪 ID
     * <p>
     * 优先使用前端传入的 {@code requestId} 请求头，实现端到端链路串联；
     * 若前端未传则自动生成。
     * </p>
     */
    private String resolveTraceId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }
        return generateTraceId();
    }

    /**
     * 生成简洁的 traceId（UUID 前 8 位）
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
