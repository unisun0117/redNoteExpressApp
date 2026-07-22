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
public class OssStsToken {

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
