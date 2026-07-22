package cn.qdm.tob.modules.system.privacy.api.mall;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.system.privacy.enums.DocType;
import cn.qdm.tob.modules.system.privacy.service.PrivacyDocService;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocSummaryVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocViewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端 — 隐私文档只读 API
 */
@RestController
@RequestMapping("/api/mall/privacy/docs")
@RequiredArgsConstructor
public class PrivacyDocMallController {

    private final PrivacyDocService docService;

    @GetMapping("/published")
    @Operation(summary = "全部已发布隐私文档")
    public ResponseResult<List<PrivacyDocSummaryVO>> published() {
        return ResponseResult.success(docService.getPublishedDocs());
    }

    @GetMapping("/type")
    @Operation(summary = "按类型取当前生效文档详情")
    public ResponseResult<PrivacyDocViewVO> byType(@RequestParam DocType docType) {
        return ResponseResult.success(docService.getByDocType(docType));
    }

    @GetMapping("/privacy-policy/versions")
    @Operation(summary = "隐私政策历史版本列表")
    public ResponseResult<List<PrivacyDocSummaryVO>> versions() {
        return ResponseResult.success(docService.getVersionList());
    }

    @GetMapping("/privacy-policy/version")
    @Operation(summary = "按版本号取隐私政策详情")
    public ResponseResult<PrivacyDocViewVO> byVersion(@Parameter(description = "版本号") @RequestParam String version) {
        return ResponseResult.success(docService.getByVersion(version));
    }
}
