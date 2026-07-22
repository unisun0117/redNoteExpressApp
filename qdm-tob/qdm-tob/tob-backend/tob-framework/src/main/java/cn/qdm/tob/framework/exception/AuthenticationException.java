package cn.qdm.tob.framework.exception;

/**
 * 认证异常
 * 用于认证流程中的通用异常
 */
public class AuthenticationException extends TobServiceException {

    public AuthenticationException() {
        super();
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

    public AuthenticationException(int code, String message) {
        super(code, message);
    }

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthenticationException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public AuthenticationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
