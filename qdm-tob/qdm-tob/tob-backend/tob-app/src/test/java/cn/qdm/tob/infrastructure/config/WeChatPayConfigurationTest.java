package cn.qdm.tob.infrastructure.config;

import cn.qdm.tob.infrastructure.autoconfigure.WeChatPayProperties;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.security.PrivateKey;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("微信支付自动配置")
class WeChatPayConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(WeChatPayConfiguration.class, PropertiesConfig.class);

    @EnableConfigurationProperties(WeChatPayProperties.class)
    static class PropertiesConfig {
    }

    @Test
    @DisplayName("mch-id 缺失时跳过自动配置")
    void shouldSkipAutoConfigurationWhenMchIdMissing() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(Config.class);
            assertThat(context).doesNotHaveBean(NotificationParser.class);
            assertThat(context).doesNotHaveBean(JsapiService.class);
            assertThat(context).doesNotHaveBean(PrivateKey.class);
        });
    }

    @Test
    @DisplayName("mch-id 存在时触发自动配置 (私钥文件不存在导致 context 启动失败)")
    void shouldAttemptConfigWhenMchIdPresent() {
        contextRunner
                .withPropertyValues(
                        "wechat.pay.mch-id=1000001",
                        "wechat.pay.api-v3-key=testkey12345678901234567",
                        "wechat.pay.private-key-path=/tmp/nonexistent_key.pem",
                        "wechat.pay.merchant-serial-number=CERT001"
                )
                .run(context -> {
                    // Context 尝试加载不存在的私钥文件会失败。
                    // 这证明条件触发器生效了——没有 mch-id 时不会创建 Config bean。
                    assertThat(context).hasFailed();
                });
    }

    @Test
    @DisplayName("条件触发需要 mch-id 属性")
    void shouldNotRegisterBeansWithoutMchId() {
        contextRunner
                .withPropertyValues(
                        "wechat.pay.api-v3-key=hasKey",
                        "wechat.pay.private-key-path=/tmp/k.pem",
                        "wechat.pay.merchant-serial-number=CERT"
                )
                .run(context -> {
                    // 即使有其他属性，缺少 mch-id 也不会自动配置
                    assertThat(context).doesNotHaveBean(Config.class);
                });
    }
}
