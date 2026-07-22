package cn.qdm.tob.modules.customer.cst.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 小程序端 - 推荐码验证结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "推荐码验证结果")
public class ReferralCodeVerifyVO {

    @Schema(description = "是否有效")
    private Boolean valid;

    @Schema(description = "业务员姓名")
    private String salespersonName;

    @Schema(description = "错误信息")
    private String message;
}
