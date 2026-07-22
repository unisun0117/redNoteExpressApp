package cn.qdm.tob.infrastructure.config;

import cn.qdm.tob.infrastructure.autoconfigure.WeChatPayProperties;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.cipher.AeadAesCipher;
import com.wechat.pay.java.core.cipher.AeadCipher;
import com.wechat.pay.java.core.cipher.RSAVerifier;
import com.wechat.pay.java.core.cipher.Verifier;
import com.wechat.pay.java.core.http.DefaultHttpClientBuilder;
import com.wechat.pay.java.core.http.HttpClient;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.util.PemUtil;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.refund.RefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

@Slf4j
@Configuration
@EnableConfigurationProperties(WeChatPayProperties.class)
@ConditionalOnProperty(prefix = "wechat.pay", name = "mch-id")
public class WeChatPayConfiguration {

    /**
     * 微信支付 Config，基于微信支付公钥验签（非平台证书）。
     * <p>
     * 商户号 1113739543 已开通微信支付公钥。
     * 使用 {@link WechatPayPublicKeyConfig} 直接传入原始公钥，避开
     * {@link com.wechat.pay.java.core.RSAConfig.Builder#wechatPayCertificatesFromPath(String...)}
     * 仅支持 X.509 证书格式的限制。
     * </p>
     */
    @Bean
    public Config wechatPayConfig(WeChatPayProperties properties, PrivateKey merchantPrivateKey) {
        PublicKey publicKey = loadPublicKey(properties.getWechatPayPublicKeyPath());
        return new WechatPayPublicKeyConfig(
                properties.getMchId(),
                merchantPrivateKey,
                properties.getMerchantSerialNumber(),
                publicKey,
                properties.getWechatPayPublicKeyId()
        );
    }

    /**
     * 预加载商户私钥，避免每次签名时从文件重新读取。
     * <p>
     * 支持 classpath: 前缀，例如 classpath:wechat_pay/apiclient_key.pem。
     * </p>
     */
    @Bean
    public PrivateKey merchantPrivateKey(WeChatPayProperties properties) {
        return loadPrivateKey(properties.getPrivateKeyPath());
    }

    /**
     * 回调通知解析器。
     * <p>
     * 手工构建 Verifier（验签）和 AeadCipher（AES-256-GCM 解密回调报文），
     * 不以 {@link Config} bean 为依赖，因为
     * {@link com.wechat.pay.java.core.notification.NotificationConfig} 需要 apiV3Key，
     * 而 {@link WechatPayPublicKeyConfig} 只实现 Config 不实现 NotificationConfig。
     * </p>
     */
    @Bean
    public NotificationParser notificationParser(WeChatPayProperties properties) {
        PublicKey publicKey = loadPublicKey(properties.getWechatPayPublicKeyPath());
        String publicKeyId = properties.getWechatPayPublicKeyId();

        // NotificationParser 验签时按 signType 查找 verifier，解密时按 serial 查找 cipher
        // 需要同时注册 publicKeyId 和 signType 两个 key，否则回调报文验签会报
        // "no verifier corresponding to signType[WECHATPAY2-SHA256-RSA2048]"
        Verifier verifier = new RSAVerifier(publicKey, publicKeyId);
        Map<String, Verifier> verifierMap = new java.util.HashMap<>();
        verifierMap.put(publicKeyId, verifier);
        verifierMap.put("WECHATPAY2-SHA256-RSA2048", verifier);

        AeadCipher aeadCipher = new AeadAesCipher(properties.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        Map<String, AeadCipher> aeadCipherMap = new java.util.HashMap<>();
        aeadCipherMap.put(publicKeyId, aeadCipher);
        aeadCipherMap.put("AEAD_AES_256_GCM", aeadCipher);

        return new NotificationParser(verifierMap, aeadCipherMap);
    }

    /**
     * JSAPI 支付 Service（SDK 封装），用于小程序下单。
     */
    @Bean
    public JsapiService jsapiService(Config config) {
        return new JsapiService.Builder()
                .config(config)
                .build();
    }

    /**
     * 退款 Service（SDK 封装），用于申请退款和查询退款。
     */
    @Bean
    public RefundService refundService(Config config) {
        return new RefundService.Builder()
                .config(config)
                .build();
    }

    /**
     * 微信支付 HTTP 客户端，用于异常退款等 SDK 未封装的 API。
     * <p>
     * 自动处理签名（Authorization 头）和验签（响应签名验证）。
     * </p>
     */
    @Bean
    public HttpClient wechatPayHttpClient(Config config) {
        return new DefaultHttpClientBuilder()
                .config(config)
                .enableRetryMultiDomain() // 主域名故障时自动切换备域名
                .build();
    }

    /**
     * 加载商户私钥，支持 classpath: 前缀。
     */
    private static PrivateKey loadPrivateKey(String path) {
        if (path.startsWith("classpath:")) {
            String keyString = loadClasspathResourceAsString(path.substring("classpath:".length()));
            return PemUtil.loadPrivateKeyFromString(keyString);
        }
        return PemUtil.loadPrivateKeyFromPath(path);
    }

    /**
     * 加载微信支付公钥，支持 classpath: 前缀。
     */
    private static PublicKey loadPublicKey(String path) {
        if (path.startsWith("classpath:")) {
            String keyString = loadClasspathResourceAsString(path.substring("classpath:".length()));
            return PemUtil.loadPublicKeyFromString(keyString);
        }
        return PemUtil.loadPublicKeyFromPath(path);
    }

    private static String loadClasspathResourceAsString(String classpath) {
        try (InputStream in = new ClassPathResource(classpath).getInputStream()) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("无法读取 classpath 资源: " + classpath, e);
        }
    }
}
