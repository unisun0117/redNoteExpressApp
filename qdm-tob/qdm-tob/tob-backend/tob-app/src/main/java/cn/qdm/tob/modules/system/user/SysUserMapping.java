package cn.qdm.tob.modules.system.user;

import cn.qdm.tob.modules.system.user.api.internal.dto.SysUserDto;
import cn.qdm.tob.modules.system.user.domain.SysUser;
import cn.qdm.tob.modules.system.user.vo.SysUserCreationVO;
import cn.qdm.tob.modules.system.user.vo.SysUserSummaryVO;
import cn.qdm.tob.modules.system.user.vo.SysUserViewVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 客户账号 MapStruct 对象映射器（遵循 RULES.md「禁止手动编写转换代码」）
 * <p>
 * CreationVO → 实体：仅映射 realName/mobile，系统字段（status/source/wechatOpenid/
 * registeredAt/lastLoginAt/updatedAt）由 SysUserService 在映射后设置。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysUserMapping {

    /** 工厂实例（用于 service 无 spring 注入时的兜底，当前使用 spring 组件） */
    SysUserMapping INSTANCE = Mappers.getMapper(SysUserMapping.class);

    /** CreationVO → 实体 */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wechatOpenid", ignore = true)
    @Mapping(target = "wechatId", ignore = true)
    @Mapping(target = "wechatNickname", ignore = true)
    @Mapping(target = "wechatAvatar", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "registeredAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SysUser toEntity(SysUserCreationVO vo);

    /** 实体 → 列表行 VO（boundCount 本期固定0，不映射） */
    @Mapping(target = "boundCount", ignore = true)
    SysUserSummaryVO toSummary(SysUser entity);

    /** 批量实体 → 列表行 VO */
    List<SysUserSummaryVO> toSummaryList(List<SysUser> entities);

    /** 实体 → 详情 VO（boundCount 本期固定0，不映射） */
    @Mapping(target = "boundCount", ignore = true)
    SysUserViewVO toView(SysUser entity);


    SysUserDto toDto(SysUser user);
}
