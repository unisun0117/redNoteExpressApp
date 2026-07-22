package cn.qdm.tob.infrastructure.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信支付配置属性。
 * <p>
 * 对应 application.yml 中 wechat.pay 前缀的配置项。
 * 通过 {@link cn.qdm.tob.infrastructure.config.WeChatPayConfiguration} 的
 * {@code @EnableConfigurationProperties} 启用，不需要 {@code @Configuration} 注解，
 * 否则会导致 Spring 重复注册 Bean。
 * </p>
 */
@Data
@ConfigurationProperties("wechat.pay")
public class WeChatPayProperties {

    /**
     * 小程序 AppID，与下单时传入的 appid 保持一致。
     */
    private String appId;

    /**
     * 商户号，由微信支付系统生成并分配给每个商户的唯一标识符。
     */
    private String mchId;

    /**
     * APIv3 密钥，用于回调报文解密。
     */
    private String apiV3Key;

    /**
     * 商户 API 证书私钥文件路径（PKCS#8 格式 PEM 文件）。
     */
    private String privateKeyPath;

    /**
     * 商户 API 证书序列号。
     */
    private String merchantSerialNumber;

    /**
     * 微信支付公钥 ID，格式如 PUB_KEY_ID_xxxxx。
     */
    private String wechatPayPublicKeyId;

    /**
     * 微信支付公钥文件路径（PEM 格式）。
     */
    private String wechatPayPublicKeyPath;

    /**
     * 支付成功回调通知地址。
     */
    private String notifyUrl;

    /**
     * 退款结果回调通知地址。
     * <p>如果不配置，则使用商户平台退款配置中的默认回调地址。</p>
     */
    private String refundNotifyUrl;

    /**
     * 微信支付 API 主域名，默认 https://api.mch.weixin.qq.com。
     */
    private String apiDomain = "https://api.mch.weixin.qq.com";
}
