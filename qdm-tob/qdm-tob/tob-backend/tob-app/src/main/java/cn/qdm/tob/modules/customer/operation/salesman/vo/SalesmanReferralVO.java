package cn.qdm.tob.modules.customer.operation.salesman.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务员推荐码列表/详情出参
 */
@Data
@Schema(description = "业务员推荐码")
public class SalesmanReferralVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "业务员姓名")
    private String name;

    @Schema(description = "业务员手机号（脱敏）")
    private String phone;

    @Schema(description = "推荐码")
    private String referralCode;

    @Schema(description = "推荐码状态")
    private String codeStatus;

    @Schema(description = "销售客户数")
    private Integer customerCount;

    @Schema(description = "修改人")
    private String updatedBy;

    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;
}
