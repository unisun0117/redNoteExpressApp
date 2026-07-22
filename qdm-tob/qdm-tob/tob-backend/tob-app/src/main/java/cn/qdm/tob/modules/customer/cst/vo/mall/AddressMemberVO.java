package cn.qdm.tob.modules.customer.cst.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小程序端 - 地址绑定成员
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "地址绑定成员")
public class AddressMemberVO {

    @Schema(description = "绑定记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户姓名")
    private String userName;

    @Schema(description = "用户手机号")
    private String userMobile;

    @Schema(description = "绑定时间")
    private String createdAt;

    @Schema(description = "是否为管理员")
    private Boolean isAdmin;
}
