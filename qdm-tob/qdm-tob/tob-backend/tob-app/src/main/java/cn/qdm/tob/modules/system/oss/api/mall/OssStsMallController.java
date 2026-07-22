package cn.qdm.tob.modules.system.oss.api.mall;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.framework.oss.OssStsToken;
import cn.qdm.tob.framework.oss.StsPolicy;
import cn.qdm.tob.framework.oss.StsTokenGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序商城端 — OSS STS 临时凭证 API
 * <p>
 * 与 admin 端 {@code OssStsController} 使用同一个 {@link StsTokenGenerator}。
 * 路径前缀 /api/mall/oss 无需 admin 权限，面向小程序用户。
 * </p>
 */
@Tag(name = "小程序 - OSS 上传凭证")
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class OssStsMallController {

    private final StsTokenGenerator stsTokenGenerator;

    @GetMapping("/sts-token")
    @Operation(summary = "获取 OSS 上传临时凭证")
    public ResponseResult<OssStsToken> stsToken(
        @Parameter(description = "上传策略", required = true) @RequestParam StsPolicy policy) {
        return ResponseResult.success(stsTokenGenerator.generate(policy));
    }
}
