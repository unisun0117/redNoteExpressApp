package cn.qdm.tob.modules.order.refund.api.mall;

import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.autoconfigure.WeChatPayProperties;
import cn.qdm.tob.modules.order.refund.dto.AbnormalRefundRequest;
import cn.qdm.tob.modules.order.refund.dto.RefundNotifyDto;
import cn.qdm.tob.modules.order.refund.api.internal.dto.RefundRequest;
import cn.qdm.tob.modules.order.refund.service.WxRefundService;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 微信支付退款接口。
 *
 * <p>关键安全设计：
 * <ul>
 *   <li>退款金额、微信支付订单号等关键字段从后端数据库查询，不信任前端传值</li>
 *   <li>回调通知先验签、再校验商户号和金额一致性，最后处理业务</li>
 * </ul>
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/mall/refund")
@RequiredArgsConstructor
@Tag(name = "微信支付退款", description = "退款申请、查询及回调相关接口")
public class WxRefundController {


    private final WxRefundService wxRefundService;
    private final WeChatPayProperties payProperties;


    /**
     * 申请退款。
     * <p>
     * 仅接收商户订单号和退款单号，退款金额等关键字段由后端从本地订单查询，
     * 遵循「铁律三：零信任」原则。
     * </p>
     */
    @PostMapping("/apply")
    @Operation(summary = "申请退款")
    public ResponseResult<Refund> applyRefund(
            @Valid @RequestBody RefundRequest request) {

        log.info("收到退款申请, outTradeNo: {}, outRefundNo: {}", request.getOutTradeNo(), request.getOutRefundNo());

        try {
            Refund response = wxRefundService.applyRefund(
                    request.getOutTradeNo(),
                    request.getOutRefundNo(),
                    request.getRefundAmountYuan(),
                    request.getReason()
            );
            return ResponseResult.success(response);
        } catch (ServiceException e) {
            log.error("退款申请失败, outRefundNo: {}, httpStatus: {}, body: {}",
                    request.getOutRefundNo(), e.getHttpStatusCode(), e.getResponseBody());
            throw new TobServiceException(400,"退款申请失败: " + e.getResponseBody());
        } catch (ValidationException e) {
            log.error("退款申请签名验证失败, outRefundNo: {}", request.getOutRefundNo(), e);
            throw new TobServiceException(400,"签名验证失败");
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.error("退款申请前置校验失败, outRefundNo: {}, 原因: {}",
                    request.getOutRefundNo(), e.getMessage());
            throw new TobServiceException(400,e.getMessage());
        }
    }

    /**
     * 查询单笔退款。
     *
     * @param outRefundNo 商户退款单号
     */
    @GetMapping("/query/{outRefundNo}")
    @Operation(summary = "查询单笔退款（通过商户退款单号）")
    public ResponseResult<Refund> queryRefund(
            @Parameter(description = "商户退款单号") @PathVariable String outRefundNo) {

        log.info("查询退款, outRefundNo: {}", outRefundNo);

        try {
            Refund response = wxRefundService.queryRefund(outRefundNo);
            return ResponseResult.success(response);
        } catch (ServiceException e) {
            log.error("查询退款失败, outRefundNo: {}, httpStatus: {}, body: {}",
                    outRefundNo, e.getHttpStatusCode(), e.getResponseBody());
            throw new TobServiceException(400,"查询退款失败: " + e.getResponseBody());
        } catch (ValidationException e) {
            log.error("查询退款签名验证失败, outRefundNo: {}", outRefundNo, e);
            throw new TobServiceException(400,"签名验证失败");
        }
    }

    /**
     * 发起异常退款。
     */
    @PostMapping("/abnormal")
    @Operation(summary = "发起异常退款（退款状态为ABNORMAL时使用）")
    public ResponseResult<Refund> applyAbnormalRefund(
            @Valid @RequestBody AbnormalRefundRequest request) {

        log.info("发起异常退款, refundId: {}, outRefundNo: {}, type: {}",
                request.getRefundId(), request.getOutRefundNo(), request.getType());

        try {
            Refund response = wxRefundService.applyAbnormalRefund(
                    request.getRefundId(),
                    request.getOutRefundNo(),
                    request.getType(),
                    request.getBankAccount(),
                    request.getRealName(),
                    request.getBankType()
            );
            return ResponseResult.success(response);
        } catch (ServiceException e) {
            log.error("异常退款失败, refundId: {}, httpStatus: {}, body: {}",
                    request.getRefundId(), e.getHttpStatusCode(), e.getResponseBody());
            throw new TobServiceException(400,"异常退款失败: " + e.getResponseBody());
        } catch (ValidationException e) {
            log.error("异常退款签名验证失败, refundId: {}", request.getRefundId(), e);
            throw new TobServiceException(400,"签名验证失败");
        }
    }

    /**
     * 微信支付退款回调通知端点。
     * <p>
     * 由微信服务器调用，已排除 Spring Security 认证。
     * 处理策略：
     * <ul>
     *   <li>验签失败（ValidationException）→ HTTP 401，触发微信重试</li>
     *   <li>验签通过 → 校验商户号 → 校验金额 → 处理业务</li>
     *   <li>业务处理失败 → HTTP 200 + FAIL，微信不重试</li>
     * </ul>
     * </p>
     */
    @PostMapping("/notify")
    @Operation(summary = "退款结果回调通知")
    public ResponseEntity<String> refundNotify(
            @RequestBody String body,
            @RequestHeader("Wechatpay-Signature") String wechatpaySignature,
            @RequestHeader("Wechatpay-Timestamp") String wechatpayTimestamp,
            @RequestHeader("Wechatpay-Nonce") String wechatpayNonce,
            @RequestHeader("Wechatpay-Serial") String wechatpaySerial) {

        log.info("收到退款回调通知, 证书序列号: {}", wechatpaySerial);

        // 1. 验签（铁律三：零信任——先验签再处理业务）
        RefundNotifyDto notifyDto;
        try {
            notifyDto = wxRefundService.parseRefundNotify(
                    body, wechatpaySignature, wechatpayTimestamp, wechatpayNonce, wechatpaySerial);
        } catch (ValidationException e) {
            log.warn("退款回调通知签名验证失败, 证书序列号: {}, 错误: {}",
                    wechatpaySerial, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"code\":\"FAIL\",\"message\":\"signature verification failed\"}");
        }

        // 2. 校验商户号（铁律三：零信任——回调 mchid 必须与配置一致）
        if (!payProperties.getMchId().equals(notifyDto.getMchid())) {
            log.error("退款回调商户号不匹配! 配置: {}, 回调: {}",
                    payProperties.getMchId(), notifyDto.getMchid());
            return ResponseEntity.ok("{\"code\":\"FAIL\",\"message\":\"mchid mismatch\"}");
        }

        // 3. 校验退款金额（铁律二：一分钱都不能错）
        Long callbackRefundAmount = notifyDto.getAmount() != null
                ? notifyDto.getAmount().getRefund() : null;
        try {
            wxRefundService.validateCallbackAmount(
                    notifyDto.getOutRefundNo(), callbackRefundAmount);
        } catch (IllegalStateException e) {
            log.error("退款回调金额校验失败, outRefundNo: {}", notifyDto.getOutRefundNo(), e);
            return ResponseEntity.ok("{\"code\":\"FAIL\",\"message\":\"amount mismatch\"}");
        }

        // 4. 处理业务
        try {
            String refundStatus = notifyDto.getRefundStatus();
            log.info("退款回调处理, refundId: {}, refundStatus: {}, outRefundNo: {}",
                    notifyDto.getRefundId(), refundStatus, notifyDto.getOutRefundNo());

            wxRefundService.updateRefundFlow(
                    notifyDto.getOutRefundNo(), refundStatus, notifyDto.getRefundId());

            switch (refundStatus) {
                case "SUCCESS":
                    log.info("退款成功, refundId: {}, outRefundNo: {}, amount: {}分",
                            notifyDto.getRefundId(), notifyDto.getOutRefundNo(), callbackRefundAmount);
                    break;
                case "CLOSED":
                    log.warn("退款关闭, refundId: {}, outRefundNo: {}",
                            notifyDto.getRefundId(), notifyDto.getOutRefundNo());
                    break;
                case "ABNORMAL":
                    log.warn("退款异常, refundId: {}, outRefundNo: {}, 需人工介入或调用异常退款接口",
                            notifyDto.getRefundId(), notifyDto.getOutRefundNo());
                    break;
                default:
                    log.warn("未知退款状态, refundId: {}, status: {}",
                            notifyDto.getRefundId(), refundStatus);
            }

        } catch (Exception e) {
            log.error("处理退款回调通知失败, refundId: {}", notifyDto.getRefundId(), e);
            return ResponseEntity.ok("{\"code\":\"FAIL\",\"message\":\"internal processing error\"}");
        }

        return ResponseEntity.ok("{\"code\":\"SUCCESS\"}");
    }
}
