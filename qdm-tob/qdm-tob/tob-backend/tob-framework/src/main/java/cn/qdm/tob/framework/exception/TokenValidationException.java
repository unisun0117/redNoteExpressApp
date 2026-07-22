package cn.qdm.tob.framework.exception;

/**
 * Token 验证异常
 * 用于 JWT Token 验证失败的场景
 */
public class TokenValidationException extends TobServiceException {

    public TokenValidationException() {
        super();
    }

    public TokenValidationException(Throwable cause) {
        super(cause);
    }

    public TokenValidationException(int code, String message) {
        super(code, message);
    }

    public TokenValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TokenValidationException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public TokenValidationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
