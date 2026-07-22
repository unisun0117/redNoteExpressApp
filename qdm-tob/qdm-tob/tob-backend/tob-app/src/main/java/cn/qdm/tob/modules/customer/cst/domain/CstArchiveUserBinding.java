package cn.qdm.tob.modules.customer.cst.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 档案用户绑定实体
 */
@Data
@TableName("cst_archive_user_binding")
public class CstArchiveUserBinding {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户档案ID */
    private Long archiveId;

    /** 小程序用户ID */
    private Long userId;

    /** 用户姓名 */
    private String userName;

    /** 用户手机号 */
    private String userMobile;

    /** 成员角色：ADMIN/MEMBER */
    private String memberRole;

    /** 邀请码 */
    private String inviteCode;

    /** 邀请码生成时间 */
    private LocalDateTime inviteCodeCreatedAt;

    /** 绑定状态：BOUND/UNBOUND */
    private String bindingStatus;

    /** 绑定时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
