package cn.qdm.tob.client.wms;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * @author zhaoxiaoyun
 */
public class WmsClientConfiguration {
    @Bean
    public RequestInterceptor requestInterceptor(WmsProperties wmsProperties) {
        return new WmsAuthorizationInterceptor(wmsProperties);
    }

    @Bean
    public Logger.Level loggerLevel() {
        return Logger.Level.FULL;
    }
}
