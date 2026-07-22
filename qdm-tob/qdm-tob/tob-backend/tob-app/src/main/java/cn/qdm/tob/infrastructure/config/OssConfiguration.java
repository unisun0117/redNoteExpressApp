package cn.qdm.tob.infrastructure.config;

import cn.qdm.tob.framework.oss.DefaultOssUrlGenerator;
import cn.qdm.tob.framework.oss.OssProperties;
import cn.qdm.tob.framework.oss.OssUrlGenerator;
import cn.qdm.tob.framework.oss.StsTokenGenerator;
import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfiguration {
    @Bean
    @ConditionalOnMissingBean(OssUrlGenerator.class)
    public OssUrlGenerator ossUrlGenerator(OSSClient ossClient, OssProperties ossProperties) {
        return new DefaultOssUrlGenerator(ossClient, ossProperties.getBucket(), ossProperties.getBaseUrl());
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(OSSClient.class)
    public OSSClient ossClient(OssProperties ossProperties) {
        var credentialsProvider = new StaticCredentialsProvider(ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());
        return OSSClient
                .newBuilder()
                .credentialsProvider(credentialsProvider)
                .region(ossProperties.getRegion())
                .build();
    }

    @Bean
    public StsTokenGenerator stsTokenGenerator(OssProperties ossProperties) {
        return new StsTokenGenerator(ossProperties);
    }
}