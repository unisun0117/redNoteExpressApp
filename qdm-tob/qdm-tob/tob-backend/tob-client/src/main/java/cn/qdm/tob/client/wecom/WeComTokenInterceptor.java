package cn.qdm.tob.client.wecom;

import cn.qdm.tob.client.wecom.dto.WeComTokenResponseDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

/**
 * 企业微信 Token 拦截器，每次请求自动注入 access_token
 *
 * @author zhaoxiaoyun
 */
public class WeComTokenInterceptor implements RequestInterceptor {
    private static final String TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    private static final long TOKEN_TTL_SECONDS = 6900; // 7200 - 300 秒安全边际

    private final Cache<String, String> tokenCache;
    private final RestClient restClient;
    private final WeComProperties properties;

    public WeComTokenInterceptor(WeComProperties properties) {
        this.properties = properties;
        this.tokenCache = Caffeine.newBuilder()
                .expireAfterWrite(TOKEN_TTL_SECONDS, TimeUnit.SECONDS)
                .build();
        this.restClient = RestClient.create();
    }

    @Override
    public void apply(RequestTemplate template) {
        String token = tokenCache.get("access_token", key -> fetchToken());
        template.query("access_token", token);
    }

    private String fetchToken() {
        WeComTokenResponseDTO resp = restClient.get()
                .uri(TOKEN_URL + "?corpid={corpid}&corpsecret={secret}",
                        properties.getCorpId(), properties.getCorpSecret())
                .retrieve()
                .body(WeComTokenResponseDTO.class);

        if (resp == null || !resp.isSucceed()) {
            throw new RuntimeException("获取企业微信 token 失败: " + (resp != null ? resp.getErrMsg() : "null response"));
        }
        return resp.getAccessToken();
    }
}
