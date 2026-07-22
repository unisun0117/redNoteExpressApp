package cn.qdm.tob.modules.system.menu.mapper;

import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.system.menu.enums.MenuGroup;
import cn.qdm.tob.modules.system.menu.enums.MenuType;
import cn.qdm.tob.modules.system.menu.domain.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Objects;

@Mapper
public interface SysMenuMapper extends TobBaseMapper<SysMenu> {

    /** 按编码查菜单（全局唯一） */
    default SysMenu findByCode(String code) {
        return lambdaSelect(w -> w.eq(SysMenu::getCode, code)).stream().findFirst().orElse(null);
    }

    /** 按分组查菜单（includeButtons=false 时排除按钮），按 sort 升序 */
    default List<SysMenu> listByGroup(MenuGroup group, boolean includeButtons) {
        return lambdaSelect(w -> w
                .eq(SysMenu::getMenuGroup, group)
                .ne(!includeButtons, SysMenu::getType, MenuType.BUTTON)
                .orderByAsc(SysMenu::getSort)
        );
    }

    /** 按分组和状态查所有菜单（含按钮），按 sort 升序 */
    default List<SysMenu> listByGroup(MenuGroup group, RecordStatus status) {
        return lambdaSelect(w -> w
                .eq(SysMenu::getMenuGroup, group)
                .eq(Objects.nonNull(status), SysMenu::getStatus, status)
                .orderByAsc(SysMenu::getSort)
        );
    }

    /** 按父节点和状态查子菜单，按 sort 升序 */
    default List<SysMenu> listByParentId(Long parentId, RecordStatus status) {
        return lambdaSelect(w -> w
                .eq(SysMenu::getParentId, parentId)
                .eq(Objects.nonNull(status), SysMenu::getStatus, status)
                .orderByAsc(SysMenu::getSort)
        );
    }

    /** 检查同级节点下名称是否重复 */
    default boolean nameExistsAtSameLevel(Long parentId, String name, Long excludeId) {
        return lambdaExists(w -> w
                .eq(SysMenu::getParentId, parentId)
                .eq(SysMenu::getName, name)
                .ne(excludeId != null, SysMenu::getId, excludeId)
        );
    }
}
