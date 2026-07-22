package cn.qdm.tob.client.message;

import feign.Logger;
import org.springframework.context.annotation.Bean;

/**
 * @author zhaoxiaoyun
 */
public class MessageClientConfiguration {
    @Bean
    public Logger.Level loggerLevel() {
        return Logger.Level.FULL;
    }
}
