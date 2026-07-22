package cn.qdm.tob.modules.system.user.api.internal.dto;

import cn.qdm.tob.modules.system.user.enums.UserSource;
import cn.qdm.tob.modules.system.user.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysUserDto {
    private Long id;

    private String mobile;

    private String realName;

    private String wechatOpenid;

    private String wechatId;

    private String wechatNickname;

    private String wechatAvatar;

    private UserStatus status;

    private UserSource source;

    private LocalDateTime registeredAt;

    private LocalDateTime lastLoginAt;

    private LocalDateTime updatedAt;
}
