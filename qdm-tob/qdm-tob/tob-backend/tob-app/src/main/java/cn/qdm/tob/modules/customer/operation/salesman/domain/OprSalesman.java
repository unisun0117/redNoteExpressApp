package cn.qdm.tob.modules.customer.operation.salesman.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务员推荐码实体
 */
@Data
@TableName("sys_salesman")
public class OprSalesman {

    @TableId
    private Long id;

    /** 关联后台用户ID */
    private Long userId;

    /** 推荐码（4位数字字母） */
    private String referralCode;

    /** 推荐码状态：VALID/EMPTY/INVALID */
    private String codeStatus;

    /** 创建人 */
    private String createdBy;

    /** 修改人 */
    private String updatedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 修改时间 */
    private LocalDateTime updatedAt;
}
