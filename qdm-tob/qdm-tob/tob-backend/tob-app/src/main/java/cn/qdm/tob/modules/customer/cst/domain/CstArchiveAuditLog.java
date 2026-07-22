package cn.qdm.tob.modules.customer.cst.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 档案审核历史实体
 */
@Data
@TableName("cst_archive_audit_log")
public class CstArchiveAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户档案ID */
    private Long archiveId;

    /** 操作动作：SUBMIT/APPROVE/REJECT/RE_SUBMIT/ASSIGN */
    private String action;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人类型：CUSTOMER/SALESMAN/MANAGER/ADMIN */
    private String operatorType;

    /** 操作人姓名 */
    private String operatorName;

    /** 审批意见或备注 */
    private String remark;

    /** 操作时间 */
    private LocalDateTime createdAt;
}
