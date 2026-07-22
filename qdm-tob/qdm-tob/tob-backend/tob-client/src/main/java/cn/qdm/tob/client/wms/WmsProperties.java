package cn.qdm.tob.client.wms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhaoxiaoyun
 */
@Data
@Configuration
@ConfigurationProperties("tob.client.wms")
public class WmsProperties {
    private String baseUrl;
    private String key;
    private String secret;
}
