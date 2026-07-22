package cn.qdm.tob.client.sap;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhaoxiaoyun
 */
@Data
@Configuration
@ConfigurationProperties("tob.client.sap")
public class SapProperties {
	private String baseUrl;
	private String key;
	private String secret;
}
