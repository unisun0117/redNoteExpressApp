package cn.qdm.tob.modules.system.user.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;

/**
 * 用户来源（对应 user.source）
 */
@Getter
public enum UserSource implements Describable {

    WECHAT("微信小程序注册"),
    ADMIN("管理员后端添加");

    private final String description;

    UserSource(String description) {
        this.description = description;
    }
}
