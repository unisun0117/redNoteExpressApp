package cn.qdm.tob.modules.system.operator.domain;

import cn.qdm.tob.infrastructure.base.TobBaseEntity;
import cn.qdm.tob.modules.system.operator.enums.OperatorStatus;
import cn.qdm.tob.modules.system.operator.enums.OperatorType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_operator")
public class SysOperator extends TobBaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工号（钱大妈统一工号，CAS:employeeCode） */
    private String employeeCode;

    /** 登录账号（企微号，CAS:namePinyin） */
    private String loginId;

    /** 手机号 */
    private String mobile;

    /** 姓名 */
    private String realName;

    /** 邮箱 */
    private String email;

    /** 操作员类型：ADMIN=管理员，SALESMAN=业务员 */
    private OperatorType type;

    /** 状态：ACTIVE/INACTIVE/LOCKED */
    private OperatorStatus status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginAt;

    /** 推荐码（系统自动生成，4位自增），隐性字段，不对外暴露 */
    private String recommendCode;
}
