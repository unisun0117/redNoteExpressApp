package cn.qdm.tob.modules.order.pay.service;

import cn.qdm.tob.infrastructure.autoconfigure.WeChatPayProperties;
import cn.qdm.tob.modules.customer.cst.api.internal.CstArchiveApi;
import cn.qdm.tob.modules.customer.cst.api.internal.dto.CstArchiveDTO;
import cn.qdm.tob.modules.order.account.domain.OrdAccountTransaction;
import cn.qdm.tob.modules.order.account.domain.OrdCustomerAccount;
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
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.model.TransactionAmount;
import com.wechat.pay.java.service.payments.model.TransactionPayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.PrivateKey;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("微信支付 Service")
class WxPayServiceTest {

    @Mock
    private NotificationParser notificationParser;

    @Mock
    private JsapiService jsapiService;

    @Mock
    private PrivateKey merchantPrivateKey;

    @Mock
    private SysUserApi sysUserApi;
    @Mock
    private BillNoGenerator billNoGenerator;

    @Mock
    private AccountTransactionMapper accountTransactionMapper;
    @Mock
    private CustomerAccountMapper customerAccountMapper;
    @Mock
    private CstArchiveApi cstArchiveApi;

    private WeChatPayProperties payProperties;
    private WxPayService wxPayService;

    @BeforeEach
    void setUp() {
        payProperties = new WeChatPayProperties();
        payProperties.setAppId("wxTestAppId");
        payProperties.setMchId("1234567890");
        payProperties.setNotifyUrl("https://example.com/notify");
        payProperties.setPrivateKeyPath("/path/to/key.pem");
        mockCustomerLookup();
        wxPayService = new WxPayService(notificationParser,
                jsapiService,
                merchantPrivateKey,
                payProperties,
                sysUserApi,
                accountTransactionMapper,
                customerAccountMapper,
                cstArchiveApi,
                billNoGenerator);
    }

    @Nested
    @DisplayName("createJsapiOrder")
    class CreateJsapiOrderTests {

        @Test
        @DisplayName("用户未绑定 openid 时抛出异常")
        void shouldThrowWhenUserHasNoOpenid() {
            CreateOrderRequest request = buildCreateOrderRequest(100, null);
            when(sysUserApi.getUserById(1L)).thenReturn(null);

            assertThatThrownBy(() -> wxPayService.createJsapiOrder(request, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("用户未绑定微信 openid");
        }

        @Test
        @DisplayName("用户 openid 为空字符串时抛出异常")
        void shouldThrowWhenOpenidIsEmpty() {
            CreateOrderRequest request = buildCreateOrderRequest(100, null);
            SysUserDto user = new SysUserDto();
            user.setId(1L);
            user.setWechatOpenid("");
            when(sysUserApi.getUserById(1L)).thenReturn(user);

            assertThatThrownBy(() -> wxPayService.createJsapiOrder(request, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("用户未绑定微信 openid");
        }

        @Test
        @DisplayName("成功从数据库获取 openid 并调用下单")
        void shouldUseOpenidFromDatabase() throws Exception {
            CreateOrderRequest request = buildCreateOrderRequest(100, "my_attach");

            SysUserDto user = new SysUserDto();
            user.setId(1L);
            user.setWechatOpenid("oDbOpenId");
            when(sysUserApi.getUserById(1L)).thenReturn(user);

            PrepayResponse mockResponse = new PrepayResponse();
            mockResponse.setPrepayId("wxTestPrepayId");
            when(jsapiService.prepay(any())).thenReturn(mockResponse);
            when(accountTransactionMapper.lambdaExists(any())).thenReturn(false);

            CreateOrderResponse result = wxPayService.createJsapiOrder(request, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getAppId()).isEqualTo("wxTestAppId");
            assertThat(result.getPackageStr()).isEqualTo("prepay_id=wxTestPrepayId");
            assertThat(result.getSignType()).isEqualTo("RSA");
            assertThat(result.getTimeStamp()).isNotEmpty();
            assertThat(result.getNonceStr()).hasSize(32);
            assertThat(result.getPaySign()).isNotNull();

            verify(jsapiService).prepay(any());
            verify(sysUserApi).getUserById(1L);
        }

        @Test
        @DisplayName("下单 → attach 可选字段为 null 时正常处理")
        void shouldHandleNullAttach() throws Exception {
            CreateOrderRequest request = buildCreateOrderRequest(100, null);

            SysUserDto user = new SysUserDto();
            user.setId(2L);
            user.setWechatOpenid("oUser");
            when(sysUserApi.getUserById(2L)).thenReturn(user);

            PrepayResponse mockResponse = new PrepayResponse();
            mockResponse.setPrepayId("wxPrepayId");
            when(jsapiService.prepay(any())).thenReturn(mockResponse);
            when(accountTransactionMapper.lambdaExists(any())).thenReturn(false);

            CreateOrderResponse result = wxPayService.createJsapiOrder(request, 2L);
            assertThat(result).isNotNull();
            verify(jsapiService).prepay(any());
        }
    }

    @Nested
    @DisplayName("parseNotify")
    class ParseNotifyTests {

        @Test
        @DisplayName("解析通知 → 成功返回 PayNotifyDto")
        void shouldParseNotifySuccessfully() {
            String body = "{\"id\":\"ev-001\"}";
            String signature = "sig123";
            String timestamp = "1234567890";
            String nonce = "abc123";
            String serial = "SERIAL001";

            Transaction mockTransaction = buildMockTransaction();
            when(notificationParser.parse(any(RequestParam.class), eq(Transaction.class)))
                    .thenReturn(mockTransaction);

            PayNotifyDto result = wxPayService.parseNotify(body, signature, timestamp, nonce, serial);

            assertThat(result).isNotNull();
            assertThat(result.getAppid()).isEqualTo("wxAppId");
            assertThat(result.getMchid()).isEqualTo("1234567890");
            assertThat(result.getOutTradeNo()).isEqualTo("ORDER001");
            assertThat(result.getTransactionId()).isEqualTo("420000123456");
            assertThat(result.getTradeType()).isEqualTo("JSAPI");
            assertThat(result.getTradeState()).isEqualTo("SUCCESS");
            assertThat(result.getTradeStateDesc()).isEqualTo("支付成功");
            assertThat(result.getBankType()).isEqualTo("CMC");
            assertThat(result.getOpenid()).isEqualTo("oBuyer123");
            assertThat(result.getTotalAmount()).isEqualTo(100);
            assertThat(result.getCurrency()).isEqualTo("CNY");
        }

        @Test
        @DisplayName("解析通知 → RequestParam 正确组装")
        void shouldBuildRequestParamCorrectly() {
            String body = "<xml>data</xml>";
            String signature = "wechatpay-sig";
            String timestamp = "9876543210";
            String nonce = "random_nonce";
            String serial = "CERT123";

            Transaction mockTransaction = buildMockTransaction();
            when(notificationParser.parse(any(RequestParam.class), eq(Transaction.class)))
                    .thenReturn(mockTransaction);

            wxPayService.parseNotify(body, signature, timestamp, nonce, serial);

            ArgumentCaptor<RequestParam> captor = ArgumentCaptor.forClass(RequestParam.class);
            verify(notificationParser).parse(captor.capture(), eq(Transaction.class));

            RequestParam captured = captor.getValue();
            assertThat(captured.getBody()).isEqualTo(body);
            assertThat(captured.getSerialNumber()).isEqualTo(serial);
        }

        @Test
        @DisplayName("解析通知 → amount 为 null 时不设置金额字段")
        void shouldHandleNullAmount() {
            Transaction mockTransaction = buildMockTransactionWithoutAmount();
            when(notificationParser.parse(any(RequestParam.class), eq(Transaction.class)))
                    .thenReturn(mockTransaction);

            PayNotifyDto result = wxPayService.parseNotify("body", "sig", "ts", "nonce", "serial");

            assertThat(result.getTotalAmount()).isNull();
            assertThat(result.getPayerTotalAmount()).isNull();
            assertThat(result.getCurrency()).isNull();
        }

        @Test
        @DisplayName("解析通知 → payer 为 null 时不设置 openid")
        void shouldHandleNullPayer() {
            Transaction mockTransaction = buildMockTransaction();
            mockTransaction.setPayer(null);
            when(notificationParser.parse(any(RequestParam.class), eq(Transaction.class)))
                    .thenReturn(mockTransaction);

            PayNotifyDto result = wxPayService.parseNotify("body", "sig", "ts", "nonce", "serial");

            assertThat(result.getOpenid()).isNull();
        }

        @Test
        @DisplayName("解析通知 → ValidationException 向上传播（调用方应返回 401）")
        void shouldPropagateValidationException() {
            when(notificationParser.parse(any(RequestParam.class), eq(Transaction.class)))
                    .thenThrow(new ValidationException("signature mismatch", null));

            assertThatThrownBy(() ->
                    wxPayService.parseNotify("body", "sig", "ts", "nonce", "serial"))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("createPaymentFlow")
    class CreatePaymentFlowTests {

        @Test
        @DisplayName("下单成功 → 创建流水（状态=PROCESSING）")
        void shouldCreateProcessingFlowOnOrder() {
            SysUserDto user = new SysUserDto();
            user.setId(1L); user.setWechatOpenid("oTest");
            user.setRealName("测试用户");
            when(sysUserApi.getUserById(1L)).thenReturn(user);
            PrepayResponse prepayResponse = new PrepayResponse();
            prepayResponse.setPrepayId("prepay_001");
            when(jsapiService.prepay(any(PrepayRequest.class))).thenReturn(prepayResponse);
            when(accountTransactionMapper.lambdaExists(any())).thenReturn(false);

            CreateOrderRequest req = buildCreateOrderRequest(100, null);
            wxPayService.createJsapiOrder(req, 1L);

            ArgumentCaptor<OrdAccountTransaction> captor = ArgumentCaptor.forClass(OrdAccountTransaction.class);
            verify(accountTransactionMapper).insert(captor.capture());
            OrdAccountTransaction tx = captor.getValue();
            assertThat(tx.getTransactionType()).isEqualTo(TransactionType.ORDER);
            assertThat(tx.getPaymentMethod()).isEqualTo(PaymentMethod.WECHAT);
            assertThat(tx.getAmount()).isEqualByComparingTo(new java.math.BigDecimal("1.00"));
            assertThat(tx.getBusinessNo()).isEqualTo("ORDER001");
            assertThat(tx.getStatus().name()).isEqualTo("PROCESSING");
        }

        @Test
        @DisplayName("下单失败（无 openid）→ 不创建流水")
        void shouldNotCreateFlowWhenNoOpenid() {
            when(sysUserApi.getUserById(1L)).thenReturn(null);
            assertThatThrownBy(() -> wxPayService.createJsapiOrder(buildCreateOrderRequest(100, null), 1L))
                    .isInstanceOf(IllegalStateException.class);
            verify(accountTransactionMapper, never()).insert(Collections.singleton(any()));
        }

        @Test
        @DisplayName("prepay 失败 → 流水先创建 PROCESSING 再回标 FAILED")
        void shouldCreateFlowThenMarkFailedWhenPrepayFails() {
            SysUserDto user = new SysUserDto();
            user.setId(1L); user.setWechatOpenid("oTest");
            when(sysUserApi.getUserById(1L)).thenReturn(user);
            when(accountTransactionMapper.lambdaExists(any())).thenReturn(false);
            when(jsapiService.prepay(any(PrepayRequest.class)))
                    .thenThrow(new RuntimeException("网络超时"));

            assertThatThrownBy(() -> wxPayService.createJsapiOrder(buildCreateOrderRequest(100, null), 1L))
                    .isInstanceOf(RuntimeException.class);

            // 流水先创建了
            verify(accountTransactionMapper).insert(any(OrdAccountTransaction.class));
            // 然后标记 FAILED
            verify(accountTransactionMapper).updateById(Collections.singleton(argThat(
                    tx -> tx.getStatus() == TransactionStatus.FAILED)));
        }

        @Test
        @DisplayName("商户订单号已存在 → 抛异常拒绝重复流水")
        void shouldThrowWhenBusinessNoExists() {
            SysUserDto user = new SysUserDto();
            user.setId(1L); user.setWechatOpenid("oTest");
            when(sysUserApi.getUserById(1L)).thenReturn(user);
            PrepayResponse prepayResponse = new PrepayResponse();
            prepayResponse.setPrepayId("prepay_001");
            when(jsapiService.prepay(any(PrepayRequest.class))).thenReturn(prepayResponse);
            when(accountTransactionMapper.lambdaExists(any())).thenReturn(true);

            assertThatThrownBy(() -> wxPayService.createJsapiOrder(buildCreateOrderRequest(100, null), 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("该商户订单号已存在流水记录");
        }
    }

    @Nested
    @DisplayName("updatePaymentFlow")
    class UpdatePaymentFlowTests {

        @Test
        @DisplayName("回调成功 → 金额一致，更新状态为 SUCCESS")
        void shouldUpdateFlowOnCallback() {
            OrdAccountTransaction existing = new OrdAccountTransaction();
            existing.setId(1L); existing.setTransactionNo("202607120001");
            existing.setBusinessNo("ORDER001"); existing.setStatus(TransactionStatus.PROCESSING);
            existing.setAmount(new java.math.BigDecimal("1.00")); // 100分
            when(accountTransactionMapper.lambdaSelectOne(any())).thenReturn(java.util.Optional.of(existing));

            wxPayService.updatePaymentFlow(buildPayNotifyDto("420000123456", 100, "SUCCESS"));

            ArgumentCaptor<OrdAccountTransaction> captor = ArgumentCaptor.forClass(OrdAccountTransaction.class);
            verify(accountTransactionMapper).updateById(captor.capture());
            assertThat(captor.getValue().getThirdPartyFlowNo()).isEqualTo("420000123456");
            assertThat(captor.getValue().getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        }

        @Test
        @DisplayName("回调金额与下单金额不一致 → 抛异常")
        void shouldThrowWhenAmountMismatch() {
            OrdAccountTransaction existing = new OrdAccountTransaction();
            existing.setId(1L); existing.setBusinessNo("ORDER001");
            existing.setAmount(new java.math.BigDecimal("1.00")); // 下单100分
            when(accountTransactionMapper.lambdaSelectOne(any())).thenReturn(java.util.Optional.of(existing));

            PayNotifyDto dto = buildPayNotifyDto("420000123456", 999, "SUCCESS"); // 回调999分

            assertThatThrownBy(() -> wxPayService.updatePaymentFlow(dto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("回调金额与下单金额不一致");
        }

        @Test
        @DisplayName("回调未找到对应流水 → 仅 warn 不报错")
        void shouldNotThrowWhenFlowNotFound() {
            when(accountTransactionMapper.lambdaSelectOne(any())).thenReturn(java.util.Optional.empty());
            wxPayService.updatePaymentFlow(buildPayNotifyDto("420000999", 100, "SUCCESS"));
            verify(accountTransactionMapper, never()).updateById(Collections.singleton(any()));
        }
    }

    // ===== 辅助方法 =====

    private CreateOrderRequest buildCreateOrderRequest(int amountInFen, String attach) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOutTradeNo("ORDER001");
        request.setAmountYuan(java.math.BigDecimal.valueOf(amountInFen).movePointLeft(2));
        request.setDescription("测试商品");
        request.setAttach(attach);
        request.setCustomerCode("C001");
        return request;
    }

    /** Mock 客户档案和账户查询，供 createOrderFlow 内部使用 */
    private void mockCustomerLookup() {
        CstArchiveDTO archive = new CstArchiveDTO();
        archive.setCompanyName("测试公司");
        archive.setSettleCompany("结算公司");
        when(cstArchiveApi.getArchiveByCode("C001")).thenReturn(archive);

        OrdCustomerAccount account = new OrdCustomerAccount();
        account.setId(100L);
        account.setCustomerCode("C001");
        when(customerAccountMapper.lambdaSelectOne(any())).thenReturn(java.util.Optional.of(account));
    }

    private Transaction buildMockTransaction() {
        Transaction t = new Transaction();
        t.setAppid("wxAppId");
        t.setMchid("1234567890");
        t.setOutTradeNo("ORDER001");
        t.setTransactionId("420000123456");
        t.setTradeType(Transaction.TradeTypeEnum.JSAPI);
        t.setTradeState(Transaction.TradeStateEnum.SUCCESS);
        t.setTradeStateDesc("支付成功");
        t.setBankType("CMC");
        t.setAttach("attach_data");
        t.setSuccessTime("2024-01-01T12:00:00+08:00");

        TransactionAmount amount = new TransactionAmount();
        amount.setTotal(100);
        amount.setPayerTotal(100);
        amount.setCurrency("CNY");
        t.setAmount(amount);

        TransactionPayer payer = new TransactionPayer();
        payer.setOpenid("oBuyer123");
        t.setPayer(payer);

        return t;
    }

    private PayNotifyDto buildPayNotifyDto(String transactionId, Integer totalAmount, String tradeState) {
        PayNotifyDto dto = new PayNotifyDto();
        dto.setAppid("wxAppId");
        dto.setMchid("1234567890");
        dto.setOutTradeNo("ORDER001");
        dto.setTransactionId(transactionId);
        dto.setTradeState(tradeState);
        dto.setTradeStateDesc("支付成功");
        dto.setOpenid("oBuyer123");
        dto.setTotalAmount(totalAmount);
        return dto;
    }

    private Transaction buildMockTransactionWithoutAmount() {
        Transaction t = new Transaction();
        t.setAppid("wxAppId");
        t.setMchid("1234567890");
        t.setOutTradeNo("ORDER002");
        t.setTransactionId("420000999999");
        return t;
    }
}
