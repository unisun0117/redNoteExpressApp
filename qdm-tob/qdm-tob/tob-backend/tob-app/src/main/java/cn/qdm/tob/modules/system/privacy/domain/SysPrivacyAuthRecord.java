package cn.qdm.tob.modules.system.privacy.domain;

import cn.qdm.tob.modules.system.privacy.enums.AuthType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 隐私授权记录实体（小程序端埋点：相机 / 相册 / 隐私政策同意 等）
 * <p>纯埋点记录，无需审计字段，不继承 {@code TobBaseEntity}。</p>
 */
@Data
@TableName("sys_privacy_auth_record")
public class SysPrivacyAuthRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 小程序用户 openid */
    private String openid;

    /** 手机号 */
    private String phone;

    /** 授权类型 */
    private AuthType authType;

    /** 授权时对应的文档版本号 */
    private String version;

    /** 授权时间 */
    private LocalDateTime authTime;
}
