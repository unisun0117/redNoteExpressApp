package cn.qdm.tob.infrastructure.config;

import com.wechat.pay.java.core.AbstractRSAConfig;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 基于微信支付公钥（pub_key.pem）的 Config 实现。
 * <p>
 * 微信支付 Java SDK 的 {@link com.wechat.pay.java.core.RSAConfig.Builder}
 * 仅支持 X.509 证书格式（-----BEGIN CERTIFICATE-----），
 * 而商户平台下载的微信支付公钥是原始公钥格式（-----BEGIN PUBLIC KEY-----）。
 * </p>
 * <p>
 * 本类直接调用 {@link AbstractRSAConfig} 暴露的
 * {@code (merchantId, privateKey, serialNo, publicKey, publicKeyId)} 构造函数，
 * 绕过 SDK Builder 对证书格式的限制。
 * </p>
 *
 * @see AbstractRSAConfig
 */
public class WechatPayPublicKeyConfig extends AbstractRSAConfig {

    public WechatPayPublicKeyConfig(String merchantId, PrivateKey privateKey,
                                     String merchantSerialNumber, PublicKey publicKey,
                                     String publicKeyId) {
        super(merchantId, privateKey, merchantSerialNumber, publicKey, publicKeyId);
    }
}
