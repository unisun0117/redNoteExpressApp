package cn.qdm.tob.modules.order.refund.service;

import cn.qdm.tob.infrastructure.autoconfigure.WeChatPayProperties;
import cn.qdm.tob.modules.order.account.domain.OrdAccountTransaction;
import cn.qdm.tob.modules.order.account.enums.TransactionStatus;
import cn.qdm.tob.modules.order.account.enums.TransactionType;
import cn.qdm.tob.modules.order.account.mapper.AccountTransactionMapper;
import cn.qdm.tob.modules.order.refund.dto.RefundNotifyDto;
import cn.qdm.tob.infrastructure.util.BillNoGenerator;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.http.HttpClient;
import com.wechat.pay.java.core.http.HttpMethod;
import com.wechat.pay.java.core.http.HttpRequest;
import com.wechat.pay.java.core.http.JsonRequestBody;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.core.util.GsonUtil;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付退款服务。
 *
 * <p>使用 SDK 的 {@link RefundService}（与 {@code JsapiService} 同模式）处理申请退款和查询退款，
 * 异常退款使用底层 {@link HttpClient} 调用。</p>
 *
 * @see <a href="https://pay.weixin.qq.com/doc/v3/merchant/4013071031">退款开发指引</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxRefundService {

    private static final String ABNORMAL_REFUND_PATH = "/v3/refund/domestic/refunds/%s/apply-abnormal-refund";

    /** 退款期限：支付后 365 天 */
    private static final long MAX_REFUND_DAYS = 365;
    /** 单笔订单最多部分退款次数 */
    private static final int MAX_PARTIAL_REFUND_COUNT = 50;
    /** remark 字段最大长度（ord_account_transaction.remark VARCHAR(2000)） */
    private static final int REMARK_MAX_LENGTH = 2000;

    private final RefundService refundService;
    private final HttpClient httpClient;
    private final NotificationParser notificationParser;
    private final WeChatPayProperties payProperties;
    private final AccountTransactionMapper accountTransactionMapper;
    private final BillNoGenerator billNoGenerator;

    /**
     * 申请退款。
     *
     * <p>关键安全措施：退款金额、微信支付订单号均从本地数据库查询，不信任前端传入值。</p>
     *
     * @param outTradeNo   原支付订单的商户订单号
     * @param outRefundNo  商户退款单号（唯一）
     * @param refundAmountYuan 退款金额（元），null 表示全额退款
     * @param reason       退款原因（可选）
     * @return 退款结果
     */
    public Refund applyRefund(String outTradeNo, String outRefundNo,
                               BigDecimal refundAmountYuan, String reason) {

        // ========== 1. 幂等性检查 ==========
        OrdAccountTransaction existingRefund = accountTransactionMapper.lambdaSelectOne(
                w -> w.eq(OrdAccountTransaction::getBusinessNo, outRefundNo)
                        .eq(OrdAccountTransaction::getTransactionType, TransactionType.REFUND)
        ).orElse(null);

        if (existingRefund != null) {
            log.info("退款单号已存在(幂等), outRefundNo: {}, status: {}", outRefundNo, existingRefund.getStatus());
            return queryRefund(outRefundNo);
        }

        // ========== 2. 查询原支付订单（铁律三：零信任）==========
        OrdAccountTransaction order = accountTransactionMapper.lambdaSelectOne(
                w -> w.eq(OrdAccountTransaction::getBusinessNo, outTradeNo)
                        .eq(OrdAccountTransaction::getTransactionType, TransactionType.ORDER)
        ).orElse(null);

        if (order == null) {
            throw new IllegalStateException("原支付订单不存在, outTradeNo=" + outTradeNo);
        }

        // ========== 3. 校验订单支付状态 ==========
        if (order.getStatus() != TransactionStatus.SUCCESS) {
            throw new IllegalStateException("原支付订单未支付成功, outTradeNo=" + outTradeNo
                    + ", status=" + order.getStatus() + "(" + order.getStatus().getDescription() + ")");
        }

        // ========== 4. 校验退款期限（365天）==========
        if (order.getCreatedAt() != null) {
            LocalDateTime refundDeadline = order.getCreatedAt().plusDays(MAX_REFUND_DAYS);
            if (LocalDateTime.now().isAfter(refundDeadline)) {
                throw new IllegalStateException("订单已超过退款期限(365天), outTradeNo=" + outTradeNo
                        + ", 支付时间=" + order.getCreatedAt());
            }
        }

        // ========== 5. 获取微信支付订单号（有则优先用，没有则用商户订单号兜底）==========
        String transactionId = order.getThirdPartyFlowNo();


        // ========== 6. 校验退款金额 ==========
        Long orderAmountFen = order.getAmount().movePointRight(2).longValue();

        long existingRefundCount = accountTransactionMapper.lambdaCount(
                w -> w.eq(OrdAccountTransaction::getOrderNo, outTradeNo)
                        .eq(OrdAccountTransaction::getTransactionType, TransactionType.REFUND)
                        .in(OrdAccountTransaction::getStatus,
                                TransactionStatus.PROCESSING, TransactionStatus.SUCCESS)
        );
        if (existingRefundCount >= MAX_PARTIAL_REFUND_COUNT) {
            throw new IllegalStateException("该订单退款次数已达上限(50次), outTradeNo=" + outTradeNo);
        }

        BigDecimal totalRefunded = calculateTotalRefunded(outTradeNo);
        Long remainingRefundableFen = order.getAmount().subtract(totalRefunded)
                .movePointRight(2).longValue();

        Long actualRefundAmount = refundAmountYuan != null
                ? refundAmountYuan.movePointRight(2).longValue()
                : orderAmountFen;
        if (actualRefundAmount <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
        if (actualRefundAmount > remainingRefundableFen) {
            throw new IllegalStateException("退款金额超过可退金额, outTradeNo=" + outTradeNo
                    + ", refundAmount=" + actualRefundAmount + "分"
                    + ", remainingRefundable=" + remainingRefundableFen + "分");
        }

        // ========== 7. 创建本地退款流水记录 ==========
        OrdAccountTransaction refundRecord = createRefundFlowRecord(
                order, outRefundNo, BigDecimal.valueOf(actualRefundAmount).movePointLeft(2), reason);

        // ========== 8. 调用 SDK RefundService（与 JsapiService 同模式）==========
        CreateRequest request = new CreateRequest();
        if (transactionId != null && !transactionId.isEmpty()) {
            request.setTransactionId(transactionId);
        } else {
            request.setOutTradeNo(outTradeNo);
        }
        request.setOutRefundNo(outRefundNo);
        if (reason != null && !reason.isEmpty()) {
            request.setReason(reason);
        }
        request.setNotifyUrl(payProperties.getRefundNotifyUrl());

        AmountReq amount = new AmountReq();
        amount.setRefund(actualRefundAmount);
        amount.setTotal(orderAmountFen);
        amount.setCurrency("CNY");
        request.setAmount(amount);

        log.info("申请退款, outRefundNo: {}, outTradeNo: {}, transactionId: {}, refundAmount: {}分",
                outRefundNo, outTradeNo, transactionId, actualRefundAmount);

        try {
            Refund result = refundService.create(request);
            log.info("退款申请已受理, refundId: {}, outRefundNo: {}, status: {}",
                    result.getRefundId(), outRefundNo, result.getStatus());

            refundRecord.setThirdPartyFlowNo(result.getRefundId());
            accountTransactionMapper.updateById(refundRecord);

            return result;
        } catch (ServiceException | ValidationException e) {
            refundRecord.setStatus(TransactionStatus.FAILED);
            refundRecord.setRemark(StringUtils.left(e.getMessage(), REMARK_MAX_LENGTH));
            accountTransactionMapper.updateById(refundRecord);
            log.error("退款申请API调用失败, outRefundNo: {}, 错误: {}", outRefundNo, e.getMessage());
            throw e;
        }
    }

    /**
     * 查询单笔退款（通过商户退款单号）。
     */
    public Refund queryRefund(String outRefundNo) {
        log.info("查询退款, outRefundNo: {}", outRefundNo);

        QueryByOutRefundNoRequest request = new QueryByOutRefundNoRequest();
        request.setOutRefundNo(outRefundNo);

        Refund result = refundService.queryByOutRefundNo(request);

        log.info("查询退款结果, outRefundNo: {}, status: {}, refundId: {}",
                outRefundNo, result.getStatus(), result.getRefundId());
        return result;
    }

    /**
     * 发起异常退款（SDK 未封装，用底层 HttpClient 调用）。
     */
    @SuppressWarnings("unchecked")
    public Refund applyAbnormalRefund(String refundId, String outRefundNo,
                                       String type, String bankAccount,
                                       String realName, String bankType) {
        log.info("发起异常退款, refundId: {}, outRefundNo: {}, type: {}", refundId, outRefundNo, type);

        Map<String, Object> body = new HashMap<>();
        body.put("out_refund_no", outRefundNo);
        body.put("type", type);
        if (bankType != null) {
            body.put("bank_type", bankType);
        }
        if (bankAccount != null) {
            body.put("bank_account", bankAccount);
        }
        if (realName != null) {
            body.put("real_name", realName);
        }

        String bodyJson = GsonUtil.getGson().toJson(body);
        String path = String.format(ABNORMAL_REFUND_PATH, refundId);

        HttpRequest request = new HttpRequest.Builder()
                .url(payProperties.getApiDomain() + path)
                .httpMethod(HttpMethod.POST)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .body(new JsonRequestBody.Builder().body(bodyJson).build())
                .build();

        var response = httpClient.execute(request, Refund.class);
        String requestId = response.getHeaders().getHeader("Request-ID");
        Refund result = response.getServiceResponse();

        log.info("异常退款已受理, Request-Id: {}, refundId: {}, status: {}",
                requestId, result.getRefundId(), result.getStatus());
        return result;
    }

    /**
     * 解析并验证退款结果回调通知。
     */
    public RefundNotifyDto parseRefundNotify(String body, String wechatpaySignature,
                                              String wechatpayTimestamp, String wechatpayNonce,
                                              String wechatpaySerial) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(wechatpaySerial)
                .nonce(wechatpayNonce)
                .signature(wechatpaySignature)
                .timestamp(wechatpayTimestamp)
                .body(body)
                .build();

        RefundNotifyDto notify = notificationParser.parse(requestParam, RefundNotifyDto.class);

        log.info("退款回调通知解析成功, refundId: {}, refundStatus: {}, outRefundNo: {}",
                notify.getRefundId(), notify.getRefundStatus(), notify.getOutRefundNo());
        return notify;
    }

    /**
     * 更新退款流水状态。
     */
    public void updateRefundFlow(String outRefundNo, String refundStatus, String refundId) {
        OrdAccountTransaction tx = accountTransactionMapper.lambdaSelectOne(
                        w -> w.eq(OrdAccountTransaction::getBusinessNo, outRefundNo)
                                .eq(OrdAccountTransaction::getTransactionType, TransactionType.REFUND))
                .orElse(null);
        if (tx == null) {
            log.warn("退款回调未找到对应流水, outRefundNo: {}", outRefundNo);
            return;
        }

        switch (refundStatus) {
            case "SUCCESS":
                tx.setStatus(TransactionStatus.SUCCESS);
                break;
            case "CLOSED":
                tx.setStatus(TransactionStatus.FAILED);
                tx.setRemark("微信退款关闭: 超7天余额不足或其它原因");
                break;
            case "ABNORMAL":
                tx.setRemark("微信退款异常: 用户银行卡作废/冻结,需人工处理");
                break;
            default:
                return;
        }

        if (refundId != null && !refundId.isEmpty()) {
            tx.setThirdPartyFlowNo(refundId);
        }
        accountTransactionMapper.updateById(tx);
        log.info("退款流水已更新, outRefundNo: {}, status: {}", outRefundNo, refundStatus);
    }

    /**
     * 校验回调金额与本地记录一致（铁律二：一分钱都不能错）。
     */
    public void validateCallbackAmount(String outRefundNo, Long callbackRefundAmount) {
        OrdAccountTransaction tx = accountTransactionMapper.lambdaSelectOne(
                        w -> w.eq(OrdAccountTransaction::getBusinessNo, outRefundNo)
                                .eq(OrdAccountTransaction::getTransactionType, TransactionType.REFUND))
                .orElse(null);
        if (tx == null) {
            return;
        }

        Long localRefundAmountFen = tx.getAmount().movePointRight(2).longValue();
        if (callbackRefundAmount != null && !callbackRefundAmount.equals(localRefundAmountFen)) {
            log.error("回调退款金额与本地记录不一致! outRefundNo={}, 本地={}分, 回调={}分",
                    outRefundNo, localRefundAmountFen, callbackRefundAmount);
            throw new IllegalStateException("回调退款金额与本地记录不一致，需人工介入。outRefundNo=" + outRefundNo);
        }
    }

    // ==================== 私有方法 ====================

    private BigDecimal calculateTotalRefunded(String outTradeNo) {
        return accountTransactionMapper.lambdaSelect(
                        w -> w.eq(OrdAccountTransaction::getOrderNo, outTradeNo)
                                .eq(OrdAccountTransaction::getTransactionType, TransactionType.REFUND)
                                .in(OrdAccountTransaction::getStatus,
                                        TransactionStatus.PROCESSING, TransactionStatus.SUCCESS)
                ).stream()
                .map(OrdAccountTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrdAccountTransaction createRefundFlowRecord(OrdAccountTransaction order,
                                                          String outRefundNo,
                                                          BigDecimal amountYuan,
                                                          String reason) {
        OrdAccountTransaction tx = new OrdAccountTransaction();
        tx.setTransactionNo(billNoGenerator.nextTransactionNo());
        tx.setAccountId(order.getAccountId());
        tx.setAccountType(order.getAccountType());
        tx.setCustomerCode(order.getCustomerCode());
        tx.setCustomerName(order.getCustomerName());
        tx.setSettlementAccountCode(order.getSettlementAccountCode());
        tx.setSettlementAccountName(order.getSettlementAccountName());
        tx.setPaymentMethod(order.getPaymentMethod());
        tx.setTransactionType(TransactionType.REFUND);
        tx.setAmount(amountYuan);
        tx.setBusinessNo(outRefundNo);
        tx.setOrderNo(order.getBusinessNo());
        tx.setWechatMerchantNo(payProperties.getMchId());
        tx.setDescription(reason);
        tx.setOperatorName(order.getOperatorName());
        tx.setOperatorId(order.getOperatorId());
        tx.setStatus(TransactionStatus.PROCESSING);
        tx.setCreatedAt(LocalDateTime.now());
        accountTransactionMapper.insert(tx);
        log.info("退款流水已创建(处理中), 流水号: {}, 退款单号: {}, 金额: {}元",
                tx.getTransactionNo(), outRefundNo, amountYuan);
        return tx;
    }

}
