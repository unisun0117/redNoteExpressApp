package cn.qdm.tob.framework.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TobException extends RuntimeException {
    /**
     * 全局错误码
     *
     * @see ErrorCode
     */
    private int code;

    public TobException() {
        super();
    }

    public TobException(Throwable cause) {
        super(cause);
    }

    public TobException(int code, String message) {
        super(message);
        this.code = code;
    }

    public TobException(ErrorCode errorCode) {
        this(errorCode.code(), errorCode.message());
    }

    public TobException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public TobException(ErrorCode errorCode, Throwable cause) {
        this(errorCode.code(), errorCode.message(), cause);
    }

    public int getCode() {
        return code;
    }
}
