package cn.qdm.tob.framework.exception;

public class TobServiceException extends TobException {

    /**
     * 空构造方法，避免反序列化问题
     */
    public TobServiceException() {
        super();
    }

    public TobServiceException(Throwable cause) {
        super(cause);
    }

    public TobServiceException(int code, String message) {
        super(code, message);
    }

    public TobServiceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TobServiceException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public TobServiceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
