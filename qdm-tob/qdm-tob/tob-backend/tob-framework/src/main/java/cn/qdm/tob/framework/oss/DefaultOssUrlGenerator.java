package cn.qdm.tob.framework.oss;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.PresignOptions;
import com.aliyun.sdk.service.oss2.models.GetObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 阿里云 OSS URL 生成器实现。
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultOssUrlGenerator implements OssUrlGenerator {
    private final OSSClient ossClient;
    private final String bucket;
    private final String baseUrl;

    @Override
    public String generatePresignedUrl(String key, long expiry, TimeUnit timeUnit) {
        var request = GetObjectRequest.newBuilder().bucket(bucket).key(key).build();
        var duration = Duration.of(expiry, timeUnit.toChronoUnit());
        var options = PresignOptions.newBuilder().expiration(duration).build();
        return ossClient.presign(request, options).url();
    }

    @Override
    public String generatePublicUrl(String key) {
        return UriComponentsBuilder.fromUriString(baseUrl).path(key).build().toUriString();
    }
}
