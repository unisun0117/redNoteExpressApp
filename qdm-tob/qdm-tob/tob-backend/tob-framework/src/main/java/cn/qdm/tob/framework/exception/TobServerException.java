package cn.qdm.tob.framework.exception;

public class TobServerException extends TobException {
    /**
     * 空构造方法，避免反序列化问题
     */
    public TobServerException() {
        super();
    }

    public TobServerException(Throwable cause) {
        super(cause);
    }

    public TobServerException(int code, String message) {
        super(code, message);
    }

    public TobServerException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TobServerException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public TobServerException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
