package cn.qdm.tob.modules.customer.cst.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小程序端 - 邀请码生成结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "邀请码生成结果")
public class InviteCodeVO {

    @Schema(description = "邀请码")
    private String inviteCode;

    @Schema(description = "过期时间")
    private String expireTime;
}
