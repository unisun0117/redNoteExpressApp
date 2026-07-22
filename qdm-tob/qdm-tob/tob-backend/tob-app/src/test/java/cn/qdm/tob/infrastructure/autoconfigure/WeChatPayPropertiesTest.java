package cn.qdm.tob.infrastructure.autoconfigure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("微信支付属性绑定")
class WeChatPayPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @EnableConfigurationProperties(WeChatPayProperties.class)
    static class TestConfig {
    }

    @Test
    @DisplayName("所有属性正确绑定")
    void shouldBindAllProperties() {
        contextRunner
                .withPropertyValues(
                        "wechat.pay.app-id=wx1234567890abcdef",
                        "wechat.pay.mch-id=1234567890",
                        "wechat.pay.api-v3-key=testapiv3key12345678901234567890",
                        "wechat.pay.private-key-path=/path/to/apiclient_key.pem",
                        "wechat.pay.merchant-serial-number=ABC123DEF456789",
                        "wechat.pay.wechat-pay-public-key-id=PUB_KEY_ID_001",
                        "wechat.pay.wechat-pay-public-key-path=/path/to/pub_key.pem",
                        "wechat.pay.notify-url=https://example.com/api/mall/pay/notify"
                )
                .run(context -> {
                    WeChatPayProperties props = context.getBean(WeChatPayProperties.class);
                    assertThat(props.getAppId()).isEqualTo("wx1234567890abcdef");
                    assertThat(props.getMchId()).isEqualTo("1234567890");
                    assertThat(props.getApiV3Key()).isEqualTo("testapiv3key12345678901234567890");
                    assertThat(props.getPrivateKeyPath()).isEqualTo("/path/to/apiclient_key.pem");
                    assertThat(props.getMerchantSerialNumber()).isEqualTo("ABC123DEF456789");
                    assertThat(props.getWechatPayPublicKeyId()).isEqualTo("PUB_KEY_ID_001");
                    assertThat(props.getWechatPayPublicKeyPath()).isEqualTo("/path/to/pub_key.pem");
                    assertThat(props.getNotifyUrl()).isEqualTo("https://example.com/api/mall/pay/notify");
                });
    }

    @Test
    @DisplayName("缺少所有属性时，字段为 null")
    void shouldBindNullsWhenPropertiesMissing() {
        contextRunner.run(context -> {
            WeChatPayProperties props = context.getBean(WeChatPayProperties.class);
            assertThat(props.getAppId()).isNull();
            assertThat(props.getMchId()).isNull();
            assertThat(props.getApiV3Key()).isNull();
            assertThat(props.getPrivateKeyPath()).isNull();
            assertThat(props.getMerchantSerialNumber()).isNull();
            assertThat(props.getWechatPayPublicKeyId()).isNull();
            assertThat(props.getWechatPayPublicKeyPath()).isNull();
            assertThat(props.getNotifyUrl()).isNull();
        });
    }

    @Test
    @DisplayName("部分属性绑定 — 只设置商户号和密钥")
    void shouldBindPartialProperties() {
        contextRunner
                .withPropertyValues(
                        "wechat.pay.mch-id=999888777",
                        "wechat.pay.api-v3-key=partial-key"
                )
                .run(context -> {
                    WeChatPayProperties props = context.getBean(WeChatPayProperties.class);
                    assertThat(props.getMchId()).isEqualTo("999888777");
                    assertThat(props.getApiV3Key()).isEqualTo("partial-key");
                    assertThat(props.getAppId()).isNull();
                    assertThat(props.getPrivateKeyPath()).isNull();
                    assertThat(props.getMerchantSerialNumber()).isNull();
                    assertThat(props.getNotifyUrl()).isNull();
                });
    }
}
