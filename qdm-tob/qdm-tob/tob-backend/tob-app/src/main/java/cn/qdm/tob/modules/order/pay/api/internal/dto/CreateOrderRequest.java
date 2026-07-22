package cn.qdm.tob.modules.order.pay.api.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 小程序支付下单请求 DTO。
 * <p>
 * 注意：openid 不从客户端传入，由服务端通过当前登录用户的 userId 从 SysUser 查询获取，
 * 避免 openid 被篡改造成资金错付。
 * </p>
 */
@Data
@Schema(description = "小程序支付下单请求")
public class CreateOrderRequest {

    @NotBlank(message = "商品描述不能为空")
    @Schema(description = "商品描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试商品")
    private String description;

    @NotBlank(message = "商户订单号不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_\\-|*]{6,32}$", message = "商户订单号格式不正确")
    @Schema(description = "商户订单号，6-32位，只能是数字、大小写字母、_-|*", requiredMode = Schema.RequiredMode.REQUIRED, example = "ORD20240101001")
    private String outTradeNo;

    @DecimalMin(value = "0.01", message = "订单金额必须大于0.01")
    @Schema(description = "订单总金额，单位为元（小数型）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.00")
    private BigDecimal amountYuan;

    @Schema(description = "商户自定义数据包（可选），长度不超过128字符，支付成功后回调原样返回")
    private String attach;

    @NotEmpty(message = "客户代码不能为空")
    @Schema(description = "客户代码")
    private String customerCode;

    @Schema(description = "父订单号（可选）")
    private String parentNo;

}
