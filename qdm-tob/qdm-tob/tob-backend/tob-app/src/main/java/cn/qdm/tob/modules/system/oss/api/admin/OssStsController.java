package cn.qdm.tob.modules.system.oss.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.framework.oss.OssStsToken;
import cn.qdm.tob.framework.oss.StsPolicy;
import cn.qdm.tob.framework.oss.StsTokenGenerator;
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

    private final StsTokenGenerator stsTokenGenerator;

    @GetMapping("/sts-token")
    @Operation(summary = "获取 OSS 上传临时凭证")
    public ResponseResult<OssStsToken> stsToken(@RequestParam StsPolicy policy) {
        return ResponseResult.success(stsTokenGenerator.generate(policy));
    }
}
