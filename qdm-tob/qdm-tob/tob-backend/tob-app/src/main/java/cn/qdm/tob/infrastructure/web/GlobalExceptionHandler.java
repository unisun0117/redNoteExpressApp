package cn.qdm.tob.infrastructure.web;

import cn.qdm.tob.framework.Constants;
import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.exception.TobServerException;
import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.framework.util.AssertUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 * <p>
 * 统一拦截 Controller 层抛出的所有异常，转换为 {@link ResponseResult} 格式返回。
 * 涵盖：
 * <ul>
 *   <li>业务异常（{@link TobServiceException}）— 4xx 系列</li>
 *   <li>服务端异常（{@link TobServerException}）— 5xx 系列</li>
 *   <li>参数校验异常（{@link MethodArgumentNotValidException}、{@link BindException}、{@link ConstraintViolationException}）</li>
 *   <li>Spring Security 权限异常（{@link AccessDeniedException}、{@link AuthenticationException}）</li>
 *   <li>Spring MVC 框架异常（{@link NoResourceFoundException}、{@link HttpRequestMethodNotSupportedException} 等）</li>
 *   <li>兜底未知异常（{@link Exception}）</li>
 * </ul>
 * </p>
 *
 * @author qdm-tob
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== Tob 业务异常 ====================

    /**
     * 业务逻辑异常
     * <p>
     * 由业务代码通过 {@link AssertUtils} 或手动抛出。
     * 根据异常 code 映射 HTTP 状态码，业务错误码通常在 4xx 范围。
     * </p>
     */
    @ExceptionHandler(TobServiceException.class)
    public ResponseEntity<ResponseResult<Void>> handleServiceException(TobServiceException ex) {
        log.info("业务异常 [code={}]: {}", ex.getCode(), ex.getMessage());
        return buildErrorResponse(ex.getCode(), ex.getMessage());
    }

    // ==================== Tob 服务端异常 ====================

    /**
     * 服务端内部异常
     * <p>
     * 由框架或基础设施代码抛出，通常对应不可恢复的内部错误。
     * </p>
     */
    @ExceptionHandler(TobServerException.class)
    public ResponseEntity<ResponseResult<Void>> handleServerException(TobServerException ex) {
        log.error("服务端异常 [code={}]", ex.getCode(), ex);
        return buildErrorResponse(ex.getCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ==================== 参数校验异常 ====================

    /**
     * {@code @Valid} / {@code @Validated} 校验失败（请求体 DTO）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseResult<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(ErrorCode.BAD_REQUEST.message());
        log.info("参数校验失败: {}", message);
        return buildErrorResponse(ErrorCode.BAD_REQUEST.code(), message);
    }

    /**
     * {@code @Valid} / {@code @Validated} 校验失败（GET 请求参数绑定）
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ResponseResult<Void>> handleBind(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(ErrorCode.BAD_REQUEST.message());
        log.info("参数绑定失败: {}", message);
        return buildErrorResponse(ErrorCode.BAD_REQUEST.code(), message);
    }

    /**
     * 方法级校验失败（{@code @Validated} on Service 方法参数）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseResult<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse(ErrorCode.BAD_REQUEST.message());
        log.info("方法参数校验失败: {}", message);
        return buildErrorResponse(ErrorCode.BAD_REQUEST.code(), message);
    }

    // ==================== HTTP 消息/参数异常 ====================

    /**
     * 请求体不可读（JSON 格式错误、类型不匹配等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseResult<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("请求体解析失败", ex);
        return buildErrorResponse(ErrorCode.BAD_REQUEST.code(), "请求体格式不正确");
    }

    /**
     * 缺少必填请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseResult<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.info("缺少请求参数: {}", ex.getParameterName());
        return buildErrorResponse(ErrorCode.BAD_REQUEST.code(), "缺少必填参数: " + ex.getParameterName());
    }

    // ==================== Spring Security 异常 ====================

    /**
     * 权限不足（无对应角色/权限码）
     * <p>
     * 注意：Spring Security Filter Chain 中 {@link AccessDeniedException} 会被
     * {@code AccessDeniedHandler} 拦截，通常不会到达此处。此处作为兜底处理。
     * </p>
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseResult<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.error("权限不足", ex);
        return buildErrorResponse(ErrorCode.FORBIDDEN.code(), ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    /**
     * 认证失败（未登录或 token 无效）
     * <p>
     * 注意：Spring Security Filter Chain 中 {@link AuthenticationException} 会被
     * {@code AuthenticationEntryPoint} 拦截，通常不会到达此处。此处作为兜底处理。
     * </p>
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseResult<Void>> handleAuthentication(AuthenticationException ex) {
        // Spring Security 的 AuthenticationException 没有 code，尝试从 cause 提取
        if (ex.getCause() instanceof TobServiceException te) {
            log.info("认证失败 [code={}]: {}", te.getCode(), te.getMessage());
            return buildErrorResponse(te.getCode(), te.getMessage());
        }
        log.info("认证失败: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.UNAUTHORIZED.code(), ErrorCode.UNAUTHORIZED.message(), HttpStatus.UNAUTHORIZED);
    }

    // ==================== Spring MVC 框架异常 ====================

    /**
     * 资源未找到（404）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseResult<Void>> handleNoResourceFound(NoResourceFoundException ex) {
        log.info("资源未找到: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.NOT_FOUND.code(), ErrorCode.NOT_FOUND.message(), HttpStatus.NOT_FOUND);
    }

    /**
     * HTTP 方法不支持（GET vs POST 等）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseResult<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.info("请求方法不支持: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.METHOD_NOT_ALLOWED.code(), ErrorCode.METHOD_NOT_ALLOWED.message(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    // ==================== 兜底 ====================

    /**
     * 未知异常兜底处理
     * <p>
     * 所有未被上述 handler 捕获的异常在此统一处理，返回 HTTP 500。
     * 打印完整堆栈便于排查。
     * </p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseResult<Void>> handleException(Exception ex) {
        log.error("未知系统异常", ex);
        return buildErrorResponse(ErrorCode.UNKNOWN.code(),
                ErrorCode.UNKNOWN.message(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 构建错误响应，HTTP 状态码由错误码映射
     *
     * @param code    业务错误码
     * @param message 错误提示
     */
    private ResponseEntity<ResponseResult<Void>> buildErrorResponse(int code, String message) {
        return buildErrorResponse(code, message, resolveHttpStatus(code));
    }

    /**
     * 构建错误响应
     *
     * @param code       业务错误码
     * @param message    错误提示
     * @param httpStatus HTTP 状态码
     */
    private ResponseEntity<ResponseResult<Void>> buildErrorResponse(int code, String message, HttpStatus httpStatus) {
        ResponseResult<Void> body = new ResponseResult<>(code, message);
        body.setTraceId(getTraceId());
        return new ResponseEntity<>(body, httpStatus);
    }

    /**
     * 从 MDC 获取当前请求的 traceId
     */
    private String getTraceId() {
        try {
            return MDC.get(Constants.TRACE_ID_KEY);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 根据业务错误码映射 HTTP 状态码
     */
    private static HttpStatus resolveHttpStatus(int code) {
        if (code >= 100 && code < 600) {
            HttpStatus status = HttpStatus.resolve(code);
            if (status != null) {
                return status;
            }
        }
        return HttpStatus.OK;
    }
}
