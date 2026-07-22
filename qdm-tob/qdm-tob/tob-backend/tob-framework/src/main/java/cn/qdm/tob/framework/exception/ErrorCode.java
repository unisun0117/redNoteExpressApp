package cn.qdm.tob.framework.exception;

import org.apache.commons.lang3.ArrayUtils;

import java.text.MessageFormat;

public interface ErrorCode {
    int SUCCESS_CODE = 0;
    static ErrorCode of(int code, String message, Object... args) {
        return new ErrorCodeImpl(code, ArrayUtils.isEmpty(args) ? message : MessageFormat.format(message, args));
    }

    ErrorCode SUCCESS = of(SUCCESS_CODE, "成功");
    ErrorCode BAD_REQUEST = of(400, "请求参数不正确");
    ErrorCode UNAUTHORIZED = of(401, "账号未登录或登录已失效");
    ErrorCode DEVICE_NOT_MATCH = of (402, "已在其他设备登录");
    ErrorCode FORBIDDEN = of(403, "没有该操作权限");
    ErrorCode NOT_FOUND = of(404, "请求未找到");
    ErrorCode METHOD_NOT_ALLOWED = of(405, "请求方法不正确");
    ErrorCode REQUEST_TIMEOUT= of(408 , "请求超时");
    ErrorCode LOCKED = of(423, "请求失败，请稍后重试");
    ErrorCode TOO_MANY_REQUESTS = of(429, "请求过于频繁，请稍后重试");

    // ========== 服务端错误段 ==========
    ErrorCode INTERNAL_SERVER_ERROR = of(500, "系统异常");
    ErrorCode NOT_IMPLEMENTED = of(501, "功能未实现/未开启");
    ErrorCode ERROR_CONFIGURATION = of(502, "错误的配置项");
    ErrorCode NOT_SUPPORTED = of(505, "该功能不支持/已下线");
    ErrorCode UNKNOWN = of(999, "未知错误");

    /** 错误编码 */
    int code();

    /** 错误提示 */
    String message();
}
