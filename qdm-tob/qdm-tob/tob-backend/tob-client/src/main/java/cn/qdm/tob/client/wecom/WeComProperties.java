package cn.qdm.tob.client.wecom;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 企业微信配置属性
 *
 * @author zhaoxiaoyun
 */
@Data
@Configuration
@ConfigurationProperties("tob.client.wecom")
public class WeComProperties {
    private String corpId;
    private String corpSecret;
}
