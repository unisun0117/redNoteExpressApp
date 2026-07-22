package cn.qdm.tob.framework.exception;

/**
 * @param code    错误码
 * @param message 错误提示
 */
record ErrorCodeImpl(int code, String message) implements ErrorCode {
}
