package cn.qdm.tob.infrastructure.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tob.auth.jwt")
public class JwtProperties {
    private String secret;
    private Long expiration;
}
