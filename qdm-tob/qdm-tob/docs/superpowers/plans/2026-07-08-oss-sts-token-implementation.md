# OSS STS 临时凭证分发 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 OSS STS 临时凭证分发接口，前端通过 `GET /api/admin/oss/sts-token?policy=AVATARS` 获取受限临时凭证后直传 OSS。

**Architecture:** 枚举定义上传策略（目录/大小/类型/有效期），StsService 调用阿里云 STS AssumeRole API 并传入 Session Policy 做细粒度权限控制，Controller 暴露 REST 端点。

**Tech Stack:** Spring Boot 4.0 + sts20150401 SDK (Tea DSL) + Lombok + commons-text

---

### Task 1: 添加 STS SDK 依赖

**Files:**
- Modify: `tob-backend/tob-dependencies/pom.xml`（在 `alibabacloud-oss-v2` 依赖之后插入）
- Modify: `tob-backend/tob-framework/pom.xml`（在 `<dependencies>` 内添加）

- [ ] **Step 1: 在 tob-dependencies/pom.xml 添加依赖管理**

在 `</dependencyManagement>` 关闭标签之前（第 151 行 `</dependencies>` 之前），紧跟 `alibabacloud-oss-v2` 依赖之后，添加：

```xml
            <dependency>
                <groupId>com.aliyun</groupId>
                <artifactId>sts20150401</artifactId>
                <version>1.2.0</version>
            </dependency>
```

**插入位置**：`alibabacloud-oss-v2` 依赖块之后、`</dependencies>` 之前（第 150 行后）。

需要修改的精确位置（原文件第 149-151 行）：
```xml
            <dependency>
                <groupId>com.aliyun</groupId>
                <artifactId>alibabacloud-oss-v2</artifactId>
                <version>0.5.0</version>
                <scope>compile</scope>
            </dependency>
        </dependencies>
```

替换为：
```xml
            <dependency>
                <groupId>com.aliyun</groupId>
                <artifactId>alibabacloud-oss-v2</artifactId>
                <version>0.5.0</version>
                <scope>compile</scope>
            </dependency>
            <dependency>
                <groupId>com.aliyun</groupId>
                <artifactId>sts20150401</artifactId>
                <version>1.2.0</version>
            </dependency>
        </dependencies>
```

- [ ] **Step 2: 在 tob-framework/pom.xml 添加依赖引用**

在 `tob-framework/pom.xml` 的 `<dependencies>` 块末尾（`</dependencies>` 之前），添加：

```xml
        <dependency>
            <groupId>com.aliyun</groupId>
            <artifactId>sts20150401</artifactId>
        </dependency>
```

- [ ] **Step 3: 验证依赖解析**

```bash
cd tob-backend && mvn dependency:resolve -pl tob-framework -DincludeArtifactIds=sts20150401 -q
```

Expected: BUILD SUCCESS，无错误输出。

- [ ] **Step 4: 提交**

```bash
git add tob-backend/tob-dependencies/pom.xml tob-backend/tob-framework/pom.xml
git commit -m "feat: 添加 sts20150401 SDK 依赖

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 2: 扩展 OssProperties 并注册为配置属性 Bean

**Files:**
- Modify: `tob-backend/tob-framework/src/main/java/cn/qdm/tob/framework/oss/OssProperties.java`
- Modify: `tob-backend/tob-app/src/main/java/cn/qdm/tob/TobApplication.java`

- [ ] **Step 1: 修改 OssProperties 添加 STS 字段和 @ConfigurationProperties**

将 `OssProperties.java` 完整替换为：

```java
package cn.qdm.tob.framework.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OSS 及 STS 配置属性。
 */
@Data
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
```

- [ ] **Step 2: 在 TobApplication 启用 OssProperties 配置绑定**

修改 `TobApplication.java`，添加 `@EnableConfigurationProperties` 导入和注解：

原文件顶部 import 区添加：
```java
import cn.qdm.tob.framework.oss.OssProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
```

原注解区 `@EnableFeignClients` 下方添加：
```java
@EnableConfigurationProperties(OssProperties.class)
```

修改后的 `TobApplication.java` 完整内容：
```java
package cn.qdm.tob;

import cn.qdm.tob.framework.oss.OssProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.modulith.Modulith;

/**
 * TOB应用主启动类
 */
@SpringBootApplication
@Modulith(sharedModules = {"infrastructure", "framework"})
@EnableFeignClients(basePackages = "cn.qdm.tob.client")
@EnableConfigurationProperties(OssProperties.class)
public class TobApplication {
    static void main(String[] args) {
        SpringApplication.run(TobApplication.class, args);
    }
}
```

- [ ] **Step 3: 在 application.yml 添加 OSS 配置**

在 `tob-backend/tob-app/src/main/resources/application.yml` 的 `tob:` 配置块内，`auth:` 同级添加 `oss:` 配置。在 `tob:` 下的 `auth:` 块之后添加：

```yaml
  oss:
    endpoint: ${TOB_OSS_ENDPOINT:oss-cn-hangzhou.aliyuncs.com}
    access-key-id: ${TOB_OSS_ACCESS_KEY_ID:}
    access-key-secret: ${TOB_OSS_ACCESS_KEY_SECRET:}
    bucket: ${TOB_OSS_BUCKET:qdm-tob}
    base-url: ${TOB_OSS_BASE_URL:https://qdm-tob.oss-cn-hangzhou.aliyuncs.com}
    region: ${TOB_OSS_REGION:cn-hangzhou}
    role-arn: ${TOB_OSS_ROLE_ARN:}
    sts-endpoint: ${TOB_OSS_STS_ENDPOINT:sts.cn-hangzhou.aliyuncs.com}
```

- [ ] **Step 4: 验证启动**

```bash
cd tob-backend && mvn compile -pl tob-framework,tob-app -q
```

Expected: BUILD SUCCESS。

- [ ] **Step 5: 提交**

```bash
git add tob-backend/tob-framework/src/main/java/cn/qdm/tob/framework/oss/OssProperties.java \
        tob-backend/tob-app/src/main/java/cn/qdm/tob/TobApplication.java \
        tob-backend/tob-app/src/main/resources/application.yml
git commit -m "feat: 扩展 OssProperties 支持 STS 配置，注册为 @ConfigurationProperties

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 3: 创建 StsPolicy 枚举

**Files:**
- Create: `tob-backend/tob-framework/src/main/java/cn/qdm/tob/framework/oss/StsPolicy.java`

- [ ] **Step 1: 创建枚举文件**

```java
package cn.qdm.tob.framework.oss;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

/**
 * OSS 上传策略枚举。
 * 枚举值即前端请求的策略名（如 ?policy=AVATARS）。
 * dir 支持 #{date:format} 日期占位符，运行时替换为当天日期。
 */
@Getter
@RequiredArgsConstructor
public enum StsPolicy {

    /** 用户头像上传 */
    AVATARS("uploads/avatars/#{date:yyyy-MM-dd}",
            DataSize.ofMegabytes(5),
            Duration.ofMinutes(15),
            new String[]{"image/png", "image/jpeg"}),

    /** 商品图片上传 */
    PRODUCTS("uploads/products/#{date:yyyy/MM/dd}",
            DataSize.ofMegabytes(10),
            Duration.ofMinutes(30),
            new String[]{"image/webp", "image/jpeg"}),
    ;

    /** 上传目录，支持 #{date:format} 占位符 */
    private final String dir;

    /** 文件大小上限 */
    private final DataSize maxSize;

    /** 凭证有效期 */
    private final Duration duration;

    /** 允许的 Content-Type（精确匹配列表） */
    private final String[] contentTypeIn;
}
```

- [ ] **Step 2: 编译验证**

```bash
cd tob-backend && mvn compile -pl tob-framework -q
```

Expected: BUILD SUCCESS。

- [ ] **Step 3: 提交**

```bash
git add tob-backend/tob-framework/src/main/java/cn/qdm/tob/framework/oss/StsPolicy.java
git commit -m "feat: 添加 StsPolicy 枚举定义上传策略

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 4: 创建 OssStsTokenVO

**Files:**
- Create: `tob-backend/tob-framework/src/main/java/cn/qdm/tob/framework/oss/OssStsTokenVO.java`

- [ ] **Step 1: 创建 VO 文件**

```java
package cn.qdm.tob.framework.oss;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * OSS STS 临时凭证返回对象。
 */
@Data
@Builder
@Schema(description = "OSS STS 临时凭证")
public class OssStsTokenVO {

    @Schema(description = "临时 AccessKey")
    private String accessKey;

    @Schema(description = "临时 SecretKey")
    private String secretKey;

    @Schema(description = "STS SecurityToken")
    private String securityToken;

    @Schema(description = "凭证过期时间（ISO 8601 格式）")
    private String expiration;

    @Schema(description = "上传目录限制")
    private String dir;

    @Schema(description = "文件允许最大大小（字节数）")
    private Long maxSize;
}
```

- [ ] **Step 2: 编译验证**

```bash
cd tob-backend && mvn compile -pl tob-framework -q
```

Expected: BUILD SUCCESS。

- [ ] **Step 3: 提交**

```bash
git add tob-backend/tob-framework/src/main/java/cn/qdm/tob/framework/oss/OssStsTokenVO.java
git commit -m "feat: 添加 OssStsTokenVO 返回对象

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 5: 创建 StsService

**Files:**
- Create: `tob-backend/tob-framework/src/main/java/cn/qdm/tob/framework/oss/StsService.java`

- [ ] **Step 1: 创建 StsService**

```java
package cn.qdm.tob.framework.oss;

import com.aliyun.sts20150401.Client;
import com.aliyun.sts20150401.models.AssumeRoleRequest;
import com.aliyun.sts20150401.models.AssumeRoleResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

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
@Service
@RequiredArgsConstructor
public class StsService {

    private static final Pattern DATE_PLACEHOLDER = Pattern.compile("#\\{date:([^}]+)}");

    private final OssProperties ossProperties;

    /**
     * 根据策略生成 STS 临时凭证。
     *
     * @param policy 上传策略枚举
     * @return 临时凭证 VO
     */
    public OssStsTokenVO generate(StsPolicy policy) {
        String resolvedDir = resolveDir(policy.getDir());
        String sessionPolicy = buildSessionPolicy(policy, resolvedDir);

        try {
            AssumeRoleResponse response = assumeRole(policy, sessionPolicy);
            AssumeRoleResponse.AssumeRoleResponseBodyCredentials credentials =
                    response.getBody().getCredentials();

            return OssStsTokenVO.builder()
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
     * 构建 Session Policy JSON，限制上传目录、文件大小、内容类型。
     */
    String buildSessionPolicy(StsPolicy policy, String resolvedDir) {
        String resource = String.format("acs:oss:*:*:%s/%s/*",
                ossProperties.getBucket(), resolvedDir);

        StringBuilder condition = new StringBuilder();
        // 文件大小限制
        condition.append("\"NumericLessThanEquals\":{\"oss:Content-Length\":")
                .append(policy.getMaxSize().toBytes()).append("}");

        // 文件类型限制
        if (policy.getContentTypeIn() != null && policy.getContentTypeIn().length > 0) {
            condition.append(",\"StringEqualsIgnoreCase\":{\"oss:ContentType\":[");
            for (int i = 0; i < policy.getContentTypeIn().length; i++) {
                if (i > 0) condition.append(",");
                condition.append("\"").append(policy.getContentTypeIn()[i]).append("\"");
            }
            condition.append("]}");
        }

        return String.format(
                "{\"Version\":\"1\",\"Statement\":[{\"Effect\":\"Allow\",\"Action\":[\"oss:PutObject\"],"
                        + "\"Resource\":\"%s\",\"Condition\":{%s}}]}",
                resource, condition);
    }

    /**
     * 解析目录中的日期占位符 #{date:format}。
     * 例如 "uploads/avatars/#{date:yyyy-MM-dd}" → "uploads/avatars/2026-07-09"
     */
    String resolveDir(String dirPattern) {
        if (dirPattern == null || !dirPattern.contains("#{")) {
            return dirPattern;
        }

        Matcher matcher = DATE_PLACEHOLDER.matcher(dirPattern);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String format = matcher.group(1);
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern(format));
            matcher.appendReplacement(result, Matcher.quoteReplacement(dateStr));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd tob-backend && mvn compile -pl tob-framework -q
```

Expected: BUILD SUCCESS。如果 sts20150401 SDK 的类找不到，先 `mvn dependency:resolve -pl tob-framework -q` 确保依赖下载成功。

- [ ] **Step 3: 提交**

```bash
git add tob-backend/tob-framework/src/main/java/cn/qdm/tob/framework/oss/StsService.java
git commit -m "feat: 添加 StsService 调用 STS AssumeRole 生成临时凭证

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 6: 创建 OssStsController

**Files:**
- Create: `tob-backend/tob-app/src/main/java/cn/qdm/tob/modules/system/oss/api/admin/OssStsController.java`

- [ ] **Step 1: 确保目录存在**

```bash
mkdir -p tob-backend/tob-app/src/main/java/cn/qdm/tob/modules/system/oss/api/admin
```

- [ ] **Step 2: 创建 Controller**

```java
package cn.qdm.tob.modules.system.oss.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.framework.oss.OssStsTokenVO;
import cn.qdm.tob.framework.oss.StsPolicy;
import cn.qdm.tob.framework.oss.StsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OSS 上传凭证管理 API。
 */
@Tag(name = "OSS 上传")
@RestController
@RequestMapping("/api/admin/oss")
@RequiredArgsConstructor
public class OssStsController {

    private final StsService stsService;

    @GetMapping("/sts-token")
    @Operation(summary = "获取 OSS 上传临时凭证")
    public ResponseResult<OssStsTokenVO> stsToken(@RequestParam StsPolicy policy) {
        return ResponseResult.success(stsService.generate(policy));
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
cd tob-backend && mvn compile -pl tob-app -q
```

Expected: BUILD SUCCESS。

- [ ] **Step 4: 提交**

```bash
git add tob-backend/tob-app/src/main/java/cn/qdm/tob/modules/system/oss/api/admin/OssStsController.java
git commit -m "feat: 添加 OssStsController 暴露 STS 临时凭证接口

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 7: 全量编译验证

- [ ] **Step 1: 完整编译**

```bash
cd tob-backend && mvn compile -q
```

Expected: BUILD SUCCESS，无编译错误。

- [ ] **Step 2: 检查 SpringDoc 是否会为新接口生成文档**

启动应用后访问 `http://localhost:8081/v3/api-docs`，确认出现 `/api/admin/oss/sts-token` 端点。

- [ ] **Step 3: 提交（如有修改）**

```bash
git status
# 如有任何因编译问题产生的修改，add 并 commit
```

---

## 总览

| 任务 | 操作 | 文件数 |
|------|------|--------|
| Task 1 | 添加依赖 | 2 |
| Task 2 | 扩展 OssProperties + 配置绑定 | 3 |
| Task 3 | 创建 StsPolicy 枚举 | 1 |
| Task 4 | 创建 OssStsTokenVO | 1 |
| Task 5 | 创建 StsService | 1 |
| Task 6 | 创建 OssStsController | 1 |
| Task 7 | 全量验证 | 0 |

**新增**：5 个 Java 文件
**修改**：2 个 pom.xml + 1 个 Java 配置类 + 1 个 YAML + 1 个启动类 = 5 个文件
