package cn.qdm.tob.modules.system.auth.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 认证类型枚举
 */
public enum AuthType implements Describable {
    SMS("小程序短信验证"),
    WECHAT("小程序微信登录"),
    CAS("CAS 认证"),
    WECOM("企业微信认证"),
    SIGNATURE("内网服务签名认证");

    private static final Map<String, AuthType> ENUM_MAP = Stream
            .of(AuthType.values())
            .collect(Collectors.toMap(
                    i -> i.name().toLowerCase(),
                    Function.identity())
            );

    @Getter
    private final String description;

    AuthType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static AuthType fromName(String name) {
        return Objects.isNull(name) ? null : ENUM_MAP.get(name.toLowerCase());
    }

    /** 判断是否为管理端认证类型（需要 RBAC 权限） */
    public static boolean isAdmin(String authType) {
        AuthType type = fromName(authType);
        return type == CAS || type == WECOM;
    }
}
