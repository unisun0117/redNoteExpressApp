package cn.qdm.tob.infrastructure.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("wechat")
public class WeChatProperties {
    private String appId;
    private String secret;
}
