package cn.qdm.tob.client.sap;

import feign.Logger;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;

/**
 * @author zhaoxiaoyun
 */
public class SapClientConfiguration {
    @Bean
    public RequestInterceptor requestInterceptor(SapProperties sapProperties) {
        return new BasicAuthRequestInterceptor(sapProperties.getKey(), sapProperties.getSecret(), StandardCharsets.UTF_8);
    }

    @Bean
    public Logger.Level loggerLevel() {
        return Logger.Level.FULL;
    }
}
