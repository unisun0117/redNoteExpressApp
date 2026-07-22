package cn.qdm.tob.modules.system.auth;

import cn.qdm.tob.modules.system.auth.vo.RegisterCreationVO;
import cn.qdm.tob.modules.system.user.domain.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * 注册入参 VO ↔ 实体转换（MapStruct 编译期生成，遵循 RULES.md「禁止手动编写转换代码」）
 * <p>
 * 字段映射：realName→realName、phone→mobile、wechatOpenId→wechatOpenid。
 * status / registeredAt / updatedAt 等系统字段由 RegistrationService 在映射后设置。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RegisterMapping {

    @Mapping(target = "mobile", source = "phone")
    @Mapping(target = "wechatOpenid", source = "wechatOpenId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "registeredAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SysUser toEntity(RegisterCreationVO vo);
}
