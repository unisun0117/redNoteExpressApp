package cn.qdm.tob.modules.system.menu.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;

@Getter
public enum MenuGroup implements Describable {
    WEB("Web端"),
    WECOM("企微端");

    private final String description;

    MenuGroup(String description) {
        this.description = description;
    }
}
