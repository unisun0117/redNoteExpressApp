package cn.qdm.tob.client.wecom;

import feign.RequestInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 企业微信 Feign 客户端配置
 *
 * @author zhaoxiaoyun
 */
public class WeComClientConfiguration {
    @Bean
    public RequestInterceptor weComTokenInterceptor(WeComProperties properties) {
        return new WeComTokenInterceptor(properties);
    }
}
