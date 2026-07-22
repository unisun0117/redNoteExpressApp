package cn.qdm.tob.framework.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OSS 及 STS 配置属性。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tob.oss")
public class OssProperties {
    /** OSS 访问端点 */
    private String endpoint;
    /** 后端调用 STS 所需的 AccessKey */
    private String accessKeyId;
    /** 后端调用 STS 所需的 SecretKey */
    private String accessKeySecret;
    /** 默认 Bucket 名称 */
    private String bucket;
    /** OSS 对外访问 Base URL */
    private String baseUrl;
    /** RAM 角色 ARN，格式 acs:ram::{uid}:role/{roleName} */
    private String roleArn;
    /** STS 服务端点，如 sts.cn-hangzhou.aliyuncs.com */
    private String stsEndpoint;
    /** 区域，如 cn-hangzhou */
    private String region;
}
