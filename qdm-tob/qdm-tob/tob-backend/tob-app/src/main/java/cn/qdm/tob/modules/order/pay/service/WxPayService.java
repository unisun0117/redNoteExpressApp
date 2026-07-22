package cn.qdm.tob.modules.order.pay.service;

import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.infrastructure.autoconfigure.WeChatPayProperties;
import cn.qdm.tob.modules.customer.cst.api.internal.CstArchiveApi;
import cn.qdm.tob.modules.customer.cst.api.internal.dto.CstArchiveDTO;
import cn.qdm.tob.modules.order.account.domain.OrdAccountTransaction;
import cn.qdm.tob.modules.order.account.domain.OrdCustomerAccount;
import cn.qdm.tob.modules.order.account.enums.AccountType;
import cn.qdm.tob.modules.order.account.enums.TransactionStatus;
import cn.qdm.tob.modules.order.account.enums.TransactionType;
import cn.qdm.tob.modules.order.account.mapper.AccountTransactionMapper;
import cn.qdm.tob.modules.order.account.mapper.CustomerAccountMapper;
import cn.qdm.tob.modules.order.flow.enums.PaymentMethod;
import cn.qdm.tob.modules.order.pay.api.internal.dto.CreateOrderRequest;
import cn.qdm.tob.modules.order.pay.dto.CreateOrderResponse;
import cn.qdm.tob.modules.order.pay.dto.PayNotifyDto;
import cn.qdm.tob.infrastructure.util.BillNoGenerator;
import cn.qdm.tob.modules.system.user.api.internal.SysUserApi;
import cn.qdm.tob.modules.system.user.api.internal.dto.SysUserDto;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class WxPayService {

    private static final char[] SYMBOLS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private static final String SIGN_ALGORITHM = "SHA256withRSA";

    /** 支付默认超时时间：30 分钟 */
    private static final long DEFAULT_PAY_TIMEOUT_MINUTES = 30;
    /** remark 字段最大长度（ord_account_transaction.remark VARCHAR(2000)） */
    private static final int REMARK_MAX_LENGTH = 2000;

    private final NotificationParser notificationParser;
    private final JsapiService jsapiService;
    private final PrivateKey merchantPrivateKey;
    private final WeChatPayProperties payProperties;
    private final SysUserApi sysUserApi;
    private final AccountTransactionMapper accountTransactionMapper;
    private final CustomerAccountMapper customerAccountMapper;
    private final CstArchiveApi cstArchiveApi;
    private final BillNoGenerator billNoGenerator;
    private final SecureRandom random = new SecureRandom();

    /**
     * 创建 JSAPI/小程序支付订单。
     *
     * @param req    下单请求（金额、描述、订单号等，不含 openid）
     * @param userId 当前登录用户 ID，用于从数据库查询 openid
     * @return 小程序端 wx.requestPayment 所需参数
     */
    public CreateOrderResponse createJsapiOrder(CreateOrderRequest req, Long userId) {
        // ① 从数据库获取 openid —— 禁止信任前端传值（铁律三：零信任）
        SysUserDto user = sysUserApi.getUserById(userId);
        if (user == null || user.getWechatOpenid() == null || user.getWechatOpenid().isEmpty()) {
            throw new IllegalStateException("用户未绑定微信 openid，userId=" + userId);
        }
        String openid = user.getWechatOpenid();

        log.info("正在创建微信支付订单, 商户订单号: {}, 金额: {}, 用户ID: {}",
                req.getOutTradeNo(), req.getAmountYuan(), userId);

        PrepayRequest prepayRequest = new PrepayRequest();
        prepayRequest.setAppid(payProperties.getAppId());
        prepayRequest.setMchid(payProperties.getMchId());
        prepayRequest.setDescription(req.getDescription());
        prepayRequest.setOutTradeNo(req.getOutTradeNo());
        prepayRequest.setNotifyUrl(payProperties.getNotifyUrl());

        // ② 设置支付超时（默认 30 分钟）
        String expireTime = OffsetDateTime.now(ZoneOffset.ofHours(8))
                .plusMinutes(DEFAULT_PAY_TIMEOUT_MINUTES)
                .truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        prepayRequest.setTimeExpire(expireTime);

        Amount amount = new Amount();
        amount.setTotal(req.getAmountYuan().movePointRight(2).intValue());
        amount.setCurrency("CNY");
        prepayRequest.setAmount(amount);

        Payer payer = new Payer();
        payer.setOpenid(openid);
        prepayRequest.setPayer(payer);

        if (req.getAttach() != null && !req.getAttach().isEmpty()) {
            prepayRequest.setAttach(req.getAttach());
        }

        // ③ 先创建资金流水（审计链路完整），prepay 失败时回标 FAILED
        OrdAccountTransaction flowRecord = createOrderFlow(req, user);

        PrepayResponse response;
        String prepayId;
        try {
            response = jsapiService.prepay(prepayRequest);
            prepayId = response.getPrepayId();
            log.info("微信支付订单创建成功, 预支付ID: {}, 商户订单号: {}, 商户号: {}, 金额(元): {}",
                    prepayId, req.getOutTradeNo(), payProperties.getMchId(), req.getAmountYuan());
        } catch (Exception e) {
            flowRecord.setStatus(TransactionStatus.FAILED);
            flowRecord.setRemark(StringUtils.left(e.getMessage(), REMARK_MAX_LENGTH));
            accountTransactionMapper.updateById(flowRecord);
            log.error("微信支付下单失败, 商户订单号: {}, 流水号: {}, 错误: {}",
                    req.getOutTradeNo(), flowRecord.getTransactionNo(), e.getMessage());
            throw new TobServiceException(400,"微信支付下单失败", e);
        }


        return buildPayParams(prepayId);
    }

    /** 下单时创建流水（状态=PROCESSING），返回流水记录供 prepay 失败时更新 */
    OrdAccountTransaction createOrderFlow(CreateOrderRequest req, SysUserDto user) {
        boolean exists = accountTransactionMapper.lambdaExists(
                w -> w.eq(OrdAccountTransaction::getBusinessNo, req.getOutTradeNo()));
        if (exists) {
            throw new IllegalStateException("该商户订单号已存在流水记录，outTradeNo=" + req.getOutTradeNo());
        }


        // 从客户档案和账户表获取客户信息（铁律三：零信任，不取前端传值）
        String customerCode = req.getCustomerCode();
        if (customerCode == null || customerCode.isBlank()) {
            throw new TobServiceException(ErrorCode.BAD_REQUEST.code(), "客户编码不能为空");
        }
        CstArchiveDTO archive = cstArchiveApi.getArchiveByCode(customerCode);
        if (archive == null) {
            throw new TobServiceException(ErrorCode.BAD_REQUEST.code(), "客户档案不存在: " + customerCode);
        }
        OrdCustomerAccount account = customerAccountMapper.lambdaSelectOne(
                w -> w.eq(OrdCustomerAccount::getCustomerCode, archive.getSettleCompany())).orElse(null);

        OrdCustomerAccount settlementCustomer = customerAccountMapper.lambdaSelectOne(w -> w.eq(OrdCustomerAccount::getCustomerCode, archive.getSettleCompany())).orElse(null);
        OrdAccountTransaction tx = new OrdAccountTransaction();
        tx.setTransactionNo(generateFlowNo());
        tx.setAccountId(account != null ? account.getId() : null);
        tx.setCustomerCode(customerCode);
        tx.setCustomerName(archive.getCompanyName());
        tx.setSettlementAccountCode(archive.getSettleCompany());
        tx.setSettlementAccountName(settlementCustomer != null ? archive.getSettleCompany() : null);

        tx.setAccountType(AccountType.PREPAID);
        tx.setTransactionType(TransactionType.ORDER);
        tx.setPaymentMethod(PaymentMethod.WECHAT);
        tx.setAmount(req.getAmountYuan());
        tx.setBusinessNo(req.getOutTradeNo());
        tx.setOrderNo(req.getParentNo());
        tx.setWechatMerchantNo(payProperties.getMchId());
        tx.setDescription(req.getDescription());
        String opName = user.getRealName() != null ? user.getRealName()
                : (user.getMobile() != null ? user.getMobile() : user.getWechatOpenid());
        tx.setOperatorName(opName);
        tx.setOperatorId(user.getId());
        tx.setStatus(TransactionStatus.PROCESSING);
        tx.setCreatedAt(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()));
        accountTransactionMapper.insert(tx);
        log.info("支付流水已创建(处理中), 流水号: {}, 商户订单号: {}", tx.getTransactionNo(), req.getOutTradeNo());
        return tx;
    }

    /** 微信回调时更新流水：校验金额 → 状态→SUCCESS → 写入第三方流水号 */
    public void updatePaymentFlow(PayNotifyDto dto) {
        OrdAccountTransaction tx = accountTransactionMapper.lambdaSelectOne(
                        w -> w.eq(OrdAccountTransaction::getBusinessNo, dto.getOutTradeNo()))
                .orElse(null);
        if (tx == null) {
            log.warn("回调未找到对应流水, outTradeNo: {}", dto.getOutTradeNo());
            return;
        }
        // 金额校验：回调金额必须与下单金额一致（铁律二：一分钱都不能错）
        BigDecimal callbackAmount = dto.getTotalAmount() != null
                ? BigDecimal.valueOf(dto.getTotalAmount()).movePointLeft(2)
                : null;
        if (callbackAmount != null && tx.getAmount() != null
                && callbackAmount.compareTo(tx.getAmount()) != 0) {
            log.error("回调金额与下单金额不一致! outTradeNo={}, 下单金额={}, 回调金额={}",
                    dto.getOutTradeNo(), tx.getAmount(), callbackAmount);
            throw new IllegalStateException("回调金额与下单金额不一致，需人工介入。outTradeNo=" + dto.getOutTradeNo());
        }
        tx.setThirdPartyFlowNo(dto.getTransactionId());
        tx.setStatus(TransactionStatus.SUCCESS);
        if (dto.getOpenid() != null) tx.setOperatorName(dto.getOpenid());
        accountTransactionMapper.updateById(tx);
        log.info("支付流水已更新(成功), 流水号: {}, 微信订单号: {}, 金额: {}",
                tx.getTransactionNo(), dto.getTransactionId(), tx.getAmount());
    }

    /**
     * 解析并验证微信支付回调通知。
     * <p>
     * 验签失败时 SDK 的 NotificationParser.parse() 会抛出 {@link ValidationException}，
     * 调用方应捕获该异常并返回 HTTP 401 触发微信重试。
     * </p>
     *
     * @param body               回调原始请求体
     * @param wechatpaySignature 回调签名头
     * @param wechatpayTimestamp 回调时间戳头
     * @param wechatpayNonce     回调随机数头
     * @param wechatpaySerial    回调证书序列号头
     * @return 解密后的业务数据
     * @throws ValidationException 验签或解密失败
     */
    public PayNotifyDto parseNotify(String body, String wechatpaySignature,
                                     String wechatpayTimestamp, String wechatpayNonce,
                                     String wechatpaySerial) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(wechatpaySerial)
                .nonce(wechatpayNonce)
                .signature(wechatpaySignature)
                .timestamp(wechatpayTimestamp)
                .body(body)
                .build();

        // NotificationParser.parse() 内部完成验签 + AES-256-GCM 解密
        // 验签失败会抛出 ValidationException → 调用方返回 HTTP 401
        Transaction transaction = notificationParser.parse(requestParam, Transaction.class);

        PayNotifyDto dto = new PayNotifyDto();
        dto.setAppid(transaction.getAppid());
        dto.setMchid(transaction.getMchid());
        dto.setOutTradeNo(transaction.getOutTradeNo());
        dto.setTransactionId(transaction.getTransactionId());
        dto.setTradeType(transaction.getTradeType() != null ? transaction.getTradeType().name() : null);
        dto.setTradeState(transaction.getTradeState() != null ? transaction.getTradeState().name() : null);
        dto.setTradeStateDesc(transaction.getTradeStateDesc());
        dto.setBankType(transaction.getBankType());
        dto.setAttach(transaction.getAttach());
        dto.setSuccessTime(transaction.getSuccessTime());

        if (transaction.getAmount() != null) {
            dto.setTotalAmount(transaction.getAmount().getTotal());
            dto.setPayerTotalAmount(transaction.getAmount().getPayerTotal());
            dto.setCurrency(transaction.getAmount().getCurrency());
        }

        if (transaction.getPayer() != null) {
            dto.setOpenid(transaction.getPayer().getOpenid());
        }

        log.info("回调通知解析成功, 微信订单号: {}, 交易状态: {}, 商户订单号: {}",
                dto.getTransactionId(), dto.getTradeState(), dto.getOutTradeNo());
        return dto;
    }

    /** 生成流水号（按天自增） */
    private String generateFlowNo() {
        return billNoGenerator.nextTransactionNo();
    }

    /**
     * 构建小程序端 wx.requestPayment 所需参数。
     */
    private CreateOrderResponse buildPayParams(String prepayId) {
        String appId = payProperties.getAppId();
        String timeStamp = String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = createNonce(32);
        String packageStr = "prepay_id=" + prepayId;
        String signType = "RSA";

        String signMessage = appId + "\n"
                + timeStamp + "\n"
                + nonceStr + "\n"
                + packageStr + "\n";

        String paySign = signWithPrivateKey(signMessage);

        return CreateOrderResponse.builder()
                .appId(appId)
                .timeStamp(timeStamp)
                .nonceStr(nonceStr)
                .packageStr(packageStr)
                .signType(signType)
                .paySign(paySign)
                .build();
    }

    /**
     * 使用预加载的商户私钥对签名串进行 SHA256withRSA 签名（避免每次从文件重新加载）。
     */
    private String signWithPrivateKey(String message) {
        try {
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initSign(merchantPrivateKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            byte[] sign = signature.sign();
            return Base64.getEncoder().encodeToString(sign);
        } catch (Exception e) {
            log.error("支付签名计算失败", e);
            throw new TobServiceException(400,"微信支付签名计算失败", e);
        }
    }

    /**
     * 生成指定长度的随机字符串。
     */
    private String createNonce(int length) {
        char[] buf = new char[length];
        for (int i = 0; i < length; ++i) {
            buf[i] = SYMBOLS[random.nextInt(SYMBOLS.length)];
        }
        return new String(buf);
    }
}
