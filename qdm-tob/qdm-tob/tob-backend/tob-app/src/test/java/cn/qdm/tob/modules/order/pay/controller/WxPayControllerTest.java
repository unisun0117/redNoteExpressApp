package cn.qdm.tob.modules.order.pay.controller;

import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.modules.order.pay.api.mall.WxPayController;
import cn.qdm.tob.modules.order.pay.api.internal.dto.CreateOrderRequest;
import cn.qdm.tob.modules.order.pay.dto.CreateOrderResponse;
import cn.qdm.tob.modules.order.pay.dto.PayNotifyDto;
import cn.qdm.tob.modules.order.pay.service.WxPayService;
import com.wechat.pay.java.core.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("微信支付 Controller")
class WxPayControllerTest {

    @Mock
    private WxPayService wxPayService;

    @Mock
    private Authentication authentication;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 模拟认证主体
        UserPrincipal principal = new UserPrincipal();
        principal.setUserId(1L);
        principal.setAuthType("miniprogram_wechat");
        when(authentication.getPrincipal()).thenReturn(principal);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new WxPayController(wxPayService))
                .build();
    }

    @Nested
    @DisplayName("POST /api/mall/pay/orders")
    class CreateOrderTests {

        @Test
        @DisplayName("有效请求返回 200 及支付参数（openid 由服务端从 DB 获取）")
        void shouldReturnPaymentParamsOnValidOrder() throws Exception {
            CreateOrderResponse response = CreateOrderResponse.builder()
                    .appId("wxAppId")
                    .timeStamp("1234567890")
                    .nonceStr("nonceStr123")
                    .packageStr("prepay_id=wxTestPrepayId")
                    .signType("RSA")
                    .paySign("base64SignatureValue")
                    .build();

            // Service 签名：createJsapiOrder(CreateOrderRequest, Long userId)
            when(wxPayService.createJsapiOrder(any(CreateOrderRequest.class), eq(1L)))
                    .thenReturn(response);

            // 请求体中不再包含 openid
            String requestJson = """
                    {"description":"测试商品","outTradeNo":"ORDER001","amount":100}""";

            mockMvc.perform(post("/api/mall/pay/orders")
                            .principal(authentication)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.appId").value("wxAppId"))
                    .andExpect(jsonPath("$.data.timeStamp").value("1234567890"))
                    .andExpect(jsonPath("$.data.nonceStr").value("nonceStr123"))
                    .andExpect(jsonPath("$.data.packageStr").value("prepay_id=wxTestPrepayId"))
                    .andExpect(jsonPath("$.data.signType").value("RSA"))
                    .andExpect(jsonPath("$.data.paySign").value("base64SignatureValue"));

            verify(wxPayService).createJsapiOrder(any(CreateOrderRequest.class), eq(1L));
        }

        @Test
        @DisplayName("缺少必填字段时返回 400")
        void shouldReturn400OnMissingFields() throws Exception {
            String requestJson = """
                    {"outTradeNo":"","amount":0}""";

            mockMvc.perform(post("/api/mall/pay/orders")
                            .principal(authentication)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/mall/pay/notify")
    class NotifyTests {

        @Test
        @DisplayName("有效回调返回 200 + {code:SUCCESS}")
        void shouldReturnSuccessOnValidNotify() throws Exception {
            String body = "{\"id\":\"ev-001\"}";
            PayNotifyDto notifyDto = new PayNotifyDto();
            notifyDto.setOutTradeNo("ORDER001");
            notifyDto.setTransactionId("420000123456");
            notifyDto.setTradeState("SUCCESS");

            when(wxPayService.parseNotify(eq(body), eq("valid_sig"), eq("1234567890"), eq("nonce"), eq("SERIAL001")))
                    .thenReturn(notifyDto);

            mockMvc.perform(post("/api/mall/pay/notify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Wechatpay-Signature", "valid_sig")
                            .header("Wechatpay-Timestamp", "1234567890")
                            .header("Wechatpay-Nonce", "nonce")
                            .header("Wechatpay-Serial", "SERIAL001")
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(content().string("{\"code\":\"SUCCESS\"}"));
        }

        @Test
        @DisplayName("验签失败（ValidationException）返回 HTTP 401")
        void shouldReturn401OnValidationFailure() throws Exception {
            String body = "invalid data";

            when(wxPayService.parseNotify(anyString(), anyString(), anyString(), anyString(), anyString()))
                    .thenThrow(new ValidationException("signature verification failed", null));

            mockMvc.perform(post("/api/mall/pay/notify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Wechatpay-Signature", "bad_sig")
                            .header("Wechatpay-Timestamp", "1234567890")
                            .header("Wechatpay-Nonce", "nonce")
                            .header("Wechatpay-Serial", "SERIAL001")
                            .content(body))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(containsString("FAIL")));
        }

        @Test
        @DisplayName("回调头信息完整传递到 Service")
        void shouldPassHeadersToService() throws Exception {
            String body = "callback body content";
            PayNotifyDto notifyDto = new PayNotifyDto();
            notifyDto.setOutTradeNo("ORDER002");

            when(wxPayService.parseNotify(anyString(), anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(notifyDto);

            mockMvc.perform(post("/api/mall/pay/notify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Wechatpay-Signature", "header_sig_value")
                            .header("Wechatpay-Timestamp", "header_ts_value")
                            .header("Wechatpay-Nonce", "header_nonce_value")
                            .header("Wechatpay-Serial", "header_serial_value")
                            .content(body))
                    .andExpect(status().isOk());

            verify(wxPayService).parseNotify(
                    eq(body),
                    eq("header_sig_value"),
                    eq("header_ts_value"),
                    eq("header_nonce_value"),
                    eq("header_serial_value")
            );
        }
    }
}
