package cn.qdm.tob.framework.oss;

import com.aliyun.sts20150401.Client;
import com.aliyun.sts20150401.models.AssumeRoleRequest;
import com.aliyun.sts20150401.models.AssumeRoleResponse;
import com.aliyun.sts20150401.models.AssumeRoleResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * STS 临时凭证服务。
 * 调用阿里云 STS AssumeRole API 获取受限临时凭证。
 */
@Slf4j
@RequiredArgsConstructor
public class StsTokenGenerator {
    private static final Pattern DATE_PLACEHOLDER = Pattern.compile("#\\{date:([^}]+)}");

    private final OssProperties ossProperties;

    /**
     * 根据策略生成 STS 临时凭证。
     *
     * @param policy 上传策略枚举
     * @return 临时凭证 VO
     */
    public OssStsToken generate(StsPolicy policy) {
        String resolvedDir = resolveDir(policy.getDir());
        String sessionPolicy = buildSessionPolicy(policy, resolvedDir);

        try {
            AssumeRoleResponse response = assumeRole(policy, sessionPolicy);
            AssumeRoleResponseBody.AssumeRoleResponseBodyCredentials credentials =
                    response.getBody().getCredentials();

            return OssStsToken.builder()
                    .accessKey(credentials.getAccessKeyId())
                    .secretKey(credentials.getAccessKeySecret())
                    .securityToken(credentials.getSecurityToken())
                    .expiration(credentials.getExpiration())
                    .dir(resolvedDir)
                    .maxSize(policy.getMaxSize().toBytes())
                    .build();
        } catch (Exception e) {
            log.error("STS AssumeRole 调用失败, policy={}", policy.name(), e);
            throw new RuntimeException("获取上传凭证失败，请稍后重试", e);
        }
    }

    /**
     * 调用 STS AssumeRole API。
     */
    private AssumeRoleResponse assumeRole(StsPolicy policy, String sessionPolicy) throws Exception {
        Config config = new Config()
                .setAccessKeyId(ossProperties.getAccessKeyId())
                .setAccessKeySecret(ossProperties.getAccessKeySecret())
                .setEndpoint(ossProperties.getStsEndpoint());

        Client client = new Client(config);

        AssumeRoleRequest request = new AssumeRoleRequest()
                .setRoleArn(ossProperties.getRoleArn())
                .setRoleSessionName("oss-upload-" + policy.name().toLowerCase(Locale.ROOT))
                .setDurationSeconds(policy.getDuration().toSeconds())
                .setPolicy(sessionPolicy);

        RuntimeOptions runtime = new RuntimeOptions();
        return client.assumeRoleWithOptions(request, runtime);
    }

    /**
     * 构建 Session Policy JSON，限制上传目录。
     * 文件大小 / 类型校验由应用层负责，此处仅限制 OSS 路径，避免使用 STS 不支持的条件键。
     */
    private String buildSessionPolicy(StsPolicy _policy, String resolvedDir) {
        String resource = String.format("acs:oss:*:*:%s/%s/*",
                ossProperties.getBucket(), resolvedDir);

        return String.format(
                "{\"Version\":\"1\",\"Statement\":[{\"Effect\":\"Allow\","
                        + "\"Action\":[\"oss:PutObject\"],"

                        + "\"Resource\":\"%s\"}]}",
                resource);
    }

    /**
     * 解析目录中的日期占位符 #{date:format}。
     * 例如 "uploads/avatars/#{date:yyyy-MM-dd}" → "uploads/avatars/2026-07-09"
     */
    private String resolveDir(String dirPattern) {
        if (!Strings.CS.contains(dirPattern, "#{")) {
            return dirPattern;
        }

        Matcher matcher = DATE_PLACEHOLDER.matcher(dirPattern);
        StringBuilder result = new StringBuilder();
        var now = LocalDate.now();
        while (matcher.find()) {
            String format = matcher.group(1);
            String dateStr = now.format(DateTimeFormatter.ofPattern(format));
            matcher.appendReplacement(result, Matcher.quoteReplacement(dateStr));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
