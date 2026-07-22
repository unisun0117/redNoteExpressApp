package cn.qdm.tob.modules.system.privacy.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;

/**
 * 小程序端授权类型
 */
@Getter
public enum AuthType implements Describable {

    CAMERA("相机"),
    ALBUM("相册"),
    PRIVACY_POLICY("隐私政策"),
    PRIVACY_SUMMARY("隐私政策摘要"),
    USER_RULES("用户管理规则与公约"),
    USER_AGREEMENT("用户协议"),
    INFO_DOWNLOAD("个人信息下载");

    private final String description;

    AuthType(String description) {
        this.description = description;
    }
}
