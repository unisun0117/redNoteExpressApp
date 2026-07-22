package cn.qdm.tob.modules.system.user.api.internal;

import cn.qdm.tob.modules.system.user.api.internal.dto.SysUserDto;

public interface SysUserApi {

    SysUserDto getUserById(Long id);
}
