package cn.qdm.tob.modules.order.pay.api.mall;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.infrastructure.security.util.SecurityUtil;
import cn.qdm.tob.modules.order.pay.api.internal.dto.CreateOrderRequest;
import cn.qdm.tob.modules.order.pay.dto.CreateOrderResponse;
import cn.qdm.tob.modules.order.pay.dto.PayNotifyDto;
import cn.qdm.tob.modules.order.pay.service.WxPayService;
import com.wechat.pay.java.core.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/mall/pay")
@RequiredArgsConstructor
@Tag(name = "微信支付", description = "小程序支付相关接口")
public class WxPayController {

    private final WxPayService wxPayService;

    /**
     * 创建小程序支付订单。
     * <p>
     * openid 从当前登录用户的 SysUser 中查询，不信任前端传值。
     * </p>
     */
    @PostMapping("/orders")
    @Operation(summary = "创建微信小程序支付订单")
    public ResponseResult<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();;

        log.info("正在创建支付订单, 商户订单号: {}, 用户ID: {}", request.getOutTradeNo(), userId);
        CreateOrderResponse response = wxPayService.createJsapiOrder(request, userId);
        return ResponseResult.success(response);
    }

    /**
     * 微信支付回调通知端点。
     * <p>
     * 由微信服务器调用，已排除 Spring Security 认证。
     * 处理策略：
     * <ul>
     *   <li>验签失败（ValidationException）→ HTTP 401，触发微信重试，不暴露错误详情</li>
     *   <li>验签通过但业务处理失败 → HTTP 200 + {"code":"FAIL","message":"..."}，微信不重试</li>
     *   <li>支付成功 → HTTP 200 + {"code":"SUCCESS"}，更新订单状态</li>
     * </ul>
     * </p>
     */
    @PostMapping("/notify")
    @Operation(summary = "微信支付回调通知")
    public ResponseEntity<String> paymentNotify(
            @RequestBody String body,
            @RequestHeader("Wechatpay-Signature") String wechatpaySignature,
            @RequestHeader("Wechatpay-Timestamp") String wechatpayTimestamp,
            @RequestHeader("Wechatpay-Nonce") String wechatpayNonce,
            @RequestHeader("Wechatpay-Serial") String wechatpaySerial) {

        log.info("收到微信支付回调通知, 证书序列号: {}", wechatpaySerial);

        PayNotifyDto notifyDto;
        try {
            notifyDto = wxPayService.parseNotify(
                    body, wechatpaySignature, wechatpayTimestamp, wechatpayNonce, wechatpaySerial);
        } catch (ValidationException e) {
            // 验签失败 → 返回 HTTP 401，微信会重试
            // 不得返回 200，否则微信 SIGNTEST 探测会判定为安全隐患
            log.warn("回调通知签名验证失败, 证书序列号: {}, 错误: {}",
                    wechatpaySerial, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"code\":\"FAIL\",\"message\":\"signature verification failed\"}");
        }

        try {
            // 支付成功 → 更新资金流水（状态 PROCESSING → SUCCESS，写入第三方流水号）
            if ("SUCCESS".equals(notifyDto.getTradeState())) {
                wxPayService.updatePaymentFlow(notifyDto);
            }

            log.info("支付回调通知处理成功, 微信订单号: {}, 交易状态: {}, 商户订单号: {}",
                    notifyDto.getTransactionId(), notifyDto.getTradeState(), notifyDto.getOutTradeNo());

        } catch (Exception e) {
            // 业务处理失败（非验签问题）→ 返回 HTTP 200 + FAIL，微信不再重试
            // 避免因业务 bug 导致微信无限重试
            log.error("处理支付回调通知失败, 商户订单号: {}", notifyDto.getOutTradeNo(), e);
            return ResponseEntity.ok("{\"code\":\"FAIL\",\"message\":\"internal processing error\"}");
        }

        // 验签通过 + 处理成功 → HTTP 200 + SUCCESS
        return ResponseEntity.ok("{\"code\":\"SUCCESS\"}");
    }
}
