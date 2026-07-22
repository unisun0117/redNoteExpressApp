package cn.qdm.tob.modules.system.user.domain;

import cn.qdm.tob.modules.system.user.enums.UserSource;
import cn.qdm.tob.modules.system.user.enums.UserStatus;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
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
