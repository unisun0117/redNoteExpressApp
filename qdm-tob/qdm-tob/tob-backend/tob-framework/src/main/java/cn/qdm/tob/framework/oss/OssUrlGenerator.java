package cn.qdm.tob.framework.oss;

import java.util.concurrent.TimeUnit;

/**
 * OSS URL 生成器接口。
 */
public interface OssUrlGenerator {

    /**
     * 生成预签名 URL
     * @param key OSS 对象 key
     * @param expiry 有效期
     * @param timeUnit 时间单位
     * @return 预签名 URL
     */
    String generatePresignedUrl(String key, long expiry, TimeUnit timeUnit);

    /**
     * 生成公开访问 URL
     * @param key OSS 对象 key
     * @return 公开 URL
     */
    String generatePublicUrl(String key);
}
