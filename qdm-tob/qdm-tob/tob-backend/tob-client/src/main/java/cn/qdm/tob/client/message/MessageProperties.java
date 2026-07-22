package cn.qdm.tob.client.message;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("tob.client.message")
public class MessageProperties {
    private String baseUrl;
    private String key;
    private String secret;
}
