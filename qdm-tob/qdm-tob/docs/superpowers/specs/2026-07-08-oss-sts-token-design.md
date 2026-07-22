# OSS STS 临时凭证分发 设计文档

> 日期：2026-07-08
> 状态：已确认

## 1. 目标

前端通过后端接口获取 STS 临时凭证（AccessKey/Secret/SecurityToken），使用阿里云 OSS SDK 直接上传文件。权限通过 Session Policy 限制到指定目录、文件大小和内容类型。

## 2. 整体流程

```
前端                    后端                        STS 服务
 │                      │                           │
 ├─ GET /api/admin/oss/sts-token?policy=AVATARS ──►│
 │                      │                           │
 │                      ├─ StsPolicy.AVATARS ───────│ 枚举查找
 │                      ├─ buildSessionPolicy() ────│ 构建限制策略
 │                      │                           │
 │                      ├─ AssumeRole(roleArn,      │
 │                      │    sessionPolicy) ────────► 获取临时凭证
 │                      │◄──── Credentials ─────────┤
 │                      │                           │
 │◄── {accessKey, secretKey, securityToken,        │
 │      expiration, dir, maxSize} ─────────────────┤
 │                      │                           │
 ├─ OSS SDK (临时凭证) ───────────────────────────────► 直接上传
```

## 3. 依赖

`tob-dependencies/pom.xml` 新增：

```xml
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>sts20150401</artifactId>
    <version>1.2.0</version>
</dependency>
```

已有依赖：`com.aliyun:alibabacloud-oss-v2:0.5.0`（预留，本次不直接使用，前端使用）。

## 4. 项目结构

### 新增文件

| 文件 | 位置 | 说明 |
|------|------|------|
| `StsPolicy.java` | `tob-framework/.../oss/` | 枚举，定义上传策略 |
| `StsService.java` | `tob-framework/.../oss/` | 核心：调用 STS AssumeRole + 构建 Session Policy |
| `OssStsTokenVO.java` | `tob-framework/.../oss/` | 返回给前端的 VO |
| `OssStsController.java` | `tob-app/.../api/admin/` | REST 端点 |

### 修改文件

| 文件 | 变更 |
|------|------|
| `tob-dependencies/pom.xml` | 新增 `sts20150401` 依赖 |
| `OssProperties.java` | 新增 `roleArn`、`stsEndpoint`、`region` 字段 |
| `tob-framework/pom.xml` | 添加 `sts20150401` 依赖引用 |
| `application.yml` | 添加 `tob.oss.role-arn` 等配置 |

## 5. 详细设计

### 5.1 StsPolicy 枚举

```java
@Getter
@RequiredArgsConstructor
public enum StsPolicy {

    AVATARS("uploads/avatars/#{date:yyyy-MM-dd}",
            DataSize.ofMegabytes(5),
            Duration.ofMinutes(15),
            new String[]{"image/png", "image/jpeg"}),

    PRODUCTS("uploads/products/#{date:yyyy/MM/dd}",
            DataSize.ofMegabytes(10),
            Duration.ofMinutes(30),
            new String[]{"image/webp", "image/jpeg"}),
    ;

    /** 上传目录，支持 #{date:format} 日期占位符 */
    private final String dir;

    /** 文件大小上限 */
    private final DataSize maxSize;

    /** 凭证有效期 */
    private final Duration duration;

    /** 允许的 Content-Type（精确匹配列表） */
    private final String[] contentTypeIn;
}
```

- 枚举值 = 前端请求的策略名（`?policy=AVATARS`）
- 新增策略只需加一行枚举值，IDE 自动提示参数
- 无需配置文件、`@ConfigurationProperties`、额外配置类

### 5.2 OssProperties 扩展

```java
@Data
public class OssProperties {
    // 现有
    private String endpoint;
    private String accessKeyId;     // 后端调用 STS 所需
    private String accessKeySecret; // 后端调用 STS 所需
    private String bucket;
    private String baseUrl;

    // 新增 STS
    private String roleArn;         // acs:ram::<uid>:role/<role-name>
    private String stsEndpoint;     // sts.cn-hangzhou.aliyuncs.com
    private String region;          // cn-hangzhou
}
```

### 5.3 Session Policy 构建

StsService 根据 StsPolicy 枚举字段构建 Session Policy JSON，传入 AssumeRole 调用：

```json
{
  "Version": "1",
  "Statement": [{
    "Effect": "Allow",
    "Action": ["oss:PutObject"],
    "Resource": "acs:oss:*:*:<bucket>/<resolved-dir>/*",
    "Condition": {
      "NumericLessThanEquals": { "oss:Content-Length": <maxSize> },
      "StringEqualsIgnoreCase": { "oss:ContentType": [<types>] }
    }
  }]
}
```

日期占位符 `#{date:format}` 在构建前由 `resolveDir()` 方法解析为实际日期。

### 5.4 StsService

```java
@Service
@RequiredArgsConstructor
public class StsService {

    private final OssProperties ossProperties;

    public OssStsTokenVO generate(StsPolicy policy) {
        String resolvedDir = resolveDir(policy.getDir());
        String sessionPolicy = buildSessionPolicy(policy, resolvedDir);
        AssumeRoleResponse response = callAssumeRole(policy, sessionPolicy);

        return OssStsTokenVO.builder()
                .accessKey(response.getCredentials().getAccessKeyId())
                .secretKey(response.getCredentials().getAccessKeySecret())
                .securityToken(response.getCredentials().getSecurityToken())
                .expiration(response.getCredentials().getExpiration())
                .dir(resolvedDir)
                .maxSize(policy.getMaxSize().toBytes())
                .build();
    }
}
```

### 5.5 OssStsTokenVO

```java
@Data
@Builder
public class OssStsTokenVO {
    private String accessKey;
    private String secretKey;
    private String securityToken;
    private String expiration;    // ISO 8601
    private String dir;
    private Long maxSize;
}
```

### 5.6 Controller

```java
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

Spring 将 `@RequestParam` 按 `StsPolicy.name()` 自动匹配（`AVATARS`、`PRODUCTS`）。

## 6. 配置示例

```yaml
tob:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    bucket: qdm-tob
    base-url: https://qdm-tob.oss-cn-hangzhou.aliyuncs.com
    region: cn-hangzhou
    role-arn: acs:ram::123456:role/oss-uploader
    sts-endpoint: sts.cn-hangzhou.aliyuncs.com
```

## 7. 与参考实现的差异

| 方面 | 参考实现 (qdm-emp) | 本方案 (qdm-tob) |
|------|-------------------|-----------------|
| 方案 | Post Policy 服务端签名直传 | STS 临时凭证分发 |
| SDK | 旧版 aliyun-sdk-oss | 新版 sts20150401 |
| 签名 | 手动 HMAC-SHA256 | STS SDK 内置，后端无需签名 |
| 前端方式 | HTTP 表单上传 | OSS SDK（更灵活） |
| 策略定义 | YAML 配置 Map | 枚举（编译检查） |
| 返回字段 | policy/signature/ossCredential 等 | accessKey/secretKey/securityToken |

## 8. 异常处理

- `StsPolicy` 参数非法 → Spring 返回 400（类型转换失败）
- STS AssumeRole 调用失败 → Service 抛异常，全局异常处理器返回 500
- `OssProperties` 必需字段缺失 → 启动时检查并 fast-fail

## 9. 安全考虑

- Session Policy 硬限制：即使前端拿到临时凭证，也只能在指定目录、大小、类型范围内操作
- 临时凭证有时效性（`Duration` 控制）
- 不暴露 RAM 用户的长效 AK/SK，只返回 STS 临时凭证
- 接口可加 `@RequirePermission` 权限控制（按需）
