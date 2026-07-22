package cn.qdm.tob.modules.system.menu;

import cn.qdm.tob.modules.system.menu.domain.SysMenu;
import cn.qdm.tob.modules.system.menu.dto.MenuSaveDTO;
import cn.qdm.tob.modules.system.menu.dto.MenuEditDTO;
import cn.qdm.tob.modules.system.menu.vo.MenuTreeNodeVO;
import cn.qdm.tob.modules.system.menu.vo.MenuViewVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MenuMapping {

    /** MenuSaveDTO -> SysMenu */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SysMenu toEntity(MenuSaveDTO dto);

    /** MenuEditDTO -> SysMenu（不更新 code/menuGroup/type，由 Service 设置） */
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "menuGroup", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SysMenu toEntity(MenuEditDTO dto);

    /** SysMenu -> MenuTreeNodeVO */
    MenuTreeNodeVO toTreeNode(SysMenu entity);

    /** List<SysMenu> -> List<MenuTreeNodeVO> */
    List<MenuTreeNodeVO> toTreeNodeList(List<SysMenu> entities);

    /** SysMenu -> MenuViewVO */
    MenuViewVO toView(SysMenu entity);
}
