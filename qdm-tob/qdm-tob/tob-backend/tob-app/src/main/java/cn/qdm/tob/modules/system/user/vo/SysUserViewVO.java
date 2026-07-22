package cn.qdm.tob.modules.system.user.vo;

import cn.qdm.tob.modules.system.user.enums.UserSource;
import cn.qdm.tob.modules.system.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户账号详情 VO（详情明文展示）
 */
@Data
@Schema(description = "客户账号详情")
public class SysUserViewVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "姓名")
    private String realName;

    @Schema(description = "手机号（明文）")
    private String mobile;

    @Schema(description = "注册来源")
    private UserSource source;

    @Schema(description = "微信openid")
    private String wechatOpenid;

    @Schema(description = "微信账号ID")
    private String wechatId;

    @Schema(description = "微信昵称")
    private String wechatNickname;

    @Schema(description = "微信头像")
    private String wechatAvatar;

    @Schema(description = "账号状态")
    private UserStatus status;

    @Schema(description = "注册时间")
    private LocalDateTime registeredAt;

    @Schema(description = "最近登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "已绑定客户档案数（依赖客户档案模块，本期固定0）")
    private Integer boundCount = 0;
}
