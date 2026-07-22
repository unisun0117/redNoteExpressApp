package cn.qdm.tob.modules.system.menu.enums;

import cn.qdm.tob.framework.Describable;
import lombok.Getter;

/**
 * 菜单类型
 * <p>MENU=一级菜单目录，PAGE=二级页面，BUTTON=三级操作按钮</p>
 */
@Getter
public enum MenuType implements Describable {

    MENU("菜单"),
    PAGE("页面"),
    BUTTON("按钮");

    private final String description;

    MenuType(String description) {
        this.description = description;
    }
}
