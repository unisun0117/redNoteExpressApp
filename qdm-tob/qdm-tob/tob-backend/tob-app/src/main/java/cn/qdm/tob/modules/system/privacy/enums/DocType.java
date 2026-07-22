package cn.qdm.tob.modules.system.privacy.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;

/**
 * 隐私文档类型
 */
@Getter
public enum DocType implements Describable {

    PRIVACY_POLICY("隐私政策"),
    PRIVACY_SUMMARY("隐私政策摘要"),
    USER_RULES("用户管理规则及公约"),
    INFO_COLLECTION("个人信息收集清单"),
    THIRD_PARTY_SHARING("第三方共享个人信息清单"),
    USER_AGREEMENT("用户协议");

    private final String description;

    DocType(String description) {
        this.description = description;
    }
}
