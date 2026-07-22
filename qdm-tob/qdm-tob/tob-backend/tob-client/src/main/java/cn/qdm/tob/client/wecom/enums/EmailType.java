package cn.qdm.tob.client.wecom.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 邮箱类型
 * @author zhaoxiaoyun
 */
public enum EmailType {
    ENTERPRISE(1), // 企业邮箱
    PERSONAL(2); // 个人邮箱

    private final int value;

    EmailType(int value) {
        this.value = value;
    }

    @JsonValue
    public Integer value() {
        return value;
    }
}
