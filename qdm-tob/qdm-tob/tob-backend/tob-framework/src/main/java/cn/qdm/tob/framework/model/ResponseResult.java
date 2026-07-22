package cn.qdm.tob.framework.model;

import cn.qdm.tob.framework.Constants;
import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.exception.TobException;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;
import java.util.Objects;

@Data
public class ResponseResult<T> implements Serializable {
    /**
     * 错误码
     *
     * @see ErrorCode#code()
     */
    private Integer code;
    /**
     * 返回数据
     */
    private T data;
    /**
     * 错误提示，用户可阅读
     *
     * @see ErrorCode#message()
     */
    private String msg;

    private String traceId;

    public ResponseResult() {
        this.code = ErrorCode.SUCCESS_CODE;
    }

    public ResponseResult(T data) {
        this.code = ErrorCode.SUCCESS_CODE;
        this.data = data;
    }

    public ResponseResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResponseResult(ErrorCode errorCode) {
        this.code = errorCode.code();
        this.msg = errorCode.message();
    }

    public boolean isSuccess() {
        return Objects.equals(this.code, ErrorCode.SUCCESS_CODE);
    }

    public static ResponseResult<Void> error(Integer code, String message) {
        ResponseResult<Void> result = new ResponseResult<>(code, message);
        result.setTraceId(getTraceIdFromMdc());
        return result;
    }

    public static ResponseResult<Void> error(ErrorCode errorCode) {
        return error(errorCode.code(), errorCode.message());
    }

    public static ResponseResult<Void> error(TobException ex) {
        return error(ex.getCode(), ex.getMessage());
    }

    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> result = new ResponseResult<>(data);
        result.setTraceId(getTraceIdFromMdc());
        return result;
    }

    public static ResponseResult<Void> success() {
        ResponseResult<Void> result = new ResponseResult<>();
        result.setTraceId(getTraceIdFromMdc());
        return result;
    }

    /**
     * 从 MDC 获取当前请求的 traceId
     */
    private static String getTraceIdFromMdc() {
        try {
            return MDC.get(Constants.TRACE_ID_KEY);
        } catch (Exception ignored) {
            return null;
        }
    }
}
