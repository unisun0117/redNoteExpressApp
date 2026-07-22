package cn.qdm.tob.modules.system.role;

import cn.qdm.tob.modules.system.role.domain.SysRole;
import cn.qdm.tob.modules.system.role.vo.RoleViewVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RoleMapping {
    RoleMapping INSTANCE = Mappers.getMapper(RoleMapping.class);

    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    RoleViewVO toView(SysRole entity);

    java.util.List<RoleViewVO> toViewList(java.util.List<SysRole> entities);
}
