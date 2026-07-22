package cn.qdm.tob.modules.system.user.vo;

import cn.qdm.tob.modules.system.user.enums.UserSource;
import cn.qdm.tob.modules.system.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户账号列表行 VO（脱敏由前端展示层处理，后端返回明文 mobile）
 */
@Data
@Schema(description = "客户账号列表行")
public class SysUserSummaryVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "姓名")
    private String realName;

    @Schema(description = "手机号（明文，前端列表脱敏）")
    private String mobile;

    @Schema(description = "注册来源")
    private UserSource source;

    @Schema(description = "微信openid（非空表示已绑定）")
    private String wechatOpenid;

    @Schema(description = "微信账号ID")
    private String wechatId;

    @Schema(description = "微信昵称")
    private String wechatNickname;

    @Schema(description = "账号状态")
    private UserStatus status;

    @Schema(description = "注册时间")
    private LocalDateTime registeredAt;

    @Schema(description = "最近登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "已绑定客户档案数（依赖客户档案模块，本期固定0）")
    private Integer boundCount = 0;
}
