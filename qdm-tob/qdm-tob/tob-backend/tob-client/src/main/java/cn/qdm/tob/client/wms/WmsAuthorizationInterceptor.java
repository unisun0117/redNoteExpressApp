package cn.qdm.tob.client.wms;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;

/**
 * @author zhaoxiaoyun
 */
public class WmsAuthorizationInterceptor implements RequestInterceptor {
    private final String key;
    private final String secret;

    public WmsAuthorizationInterceptor(WmsProperties wmsProperties) {
        this.key = wmsProperties.getKey();
        this.secret = wmsProperties.getSecret();
    }

    @Override
    public void apply(RequestTemplate template) {
        String nonce = RandomStringUtils.insecure().nextAlphanumeric(6, 10);
        String authorization = nonce + " " + DigestUtils.md5Hex(nonce + secret);
        template.header(HttpHeaders.AUTHORIZATION, authorization);
        template.header("SourceID", key);
    }
}
