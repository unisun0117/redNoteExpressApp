package cn.qdm.tob.modules.system.privacy.api.open;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.system.privacy.enums.DocType;
import cn.qdm.tob.modules.system.privacy.service.PrivacyDocService;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocSummaryVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocViewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公开隐私文档 API（无需登录，H5 直接调用）。
 * <p>
 * URL 前缀 /api/public/privacy/docs/ 已被 SecurityConfiguration 白名单覆盖。
 * </p>
 */
@Tag(name = "隐私文档-公开")
@RestController
@RequestMapping("/api/public/privacy/docs")
@RequiredArgsConstructor
public class PrivacyDocOpenController {

    private final PrivacyDocService docService;

    @GetMapping("/privacy-policy")
    @Operation(summary = "获取最新已发布隐私政策")
    public ResponseResult<PrivacyDocViewVO> latestPrivacyPolicy() {
        return ResponseResult.success(docService.getByDocType(DocType.PRIVACY_POLICY));
    }

    @GetMapping("/privacy-policy/versions")
    @Operation(summary = "隐私政策历史版本列表")
    public ResponseResult<List<PrivacyDocSummaryVO>> versions() {
        return ResponseResult.success(docService.getVersionList());
    }

    @GetMapping("/privacy-policy/version")
    @Operation(summary = "按版本号获取隐私政策")
    public ResponseResult<PrivacyDocViewVO> byVersion(
            @Parameter(description = "版本号", required = true) @RequestParam String version) {
        return ResponseResult.success(docService.getByVersion(version));
    }

    @GetMapping("/privacy-summary")
    @Operation(summary = "获取最新已发布隐私政策摘要")
    public ResponseResult<PrivacyDocViewVO> privacySummary() {
        return ResponseResult.success(docService.getByDocType(DocType.PRIVACY_SUMMARY));
    }

    @GetMapping("/user-rules")
    @Operation(summary = "获取最新已发布用户管理规则及公约")
    public ResponseResult<PrivacyDocViewVO> userRules() {
        return ResponseResult.success(docService.getByDocType(DocType.USER_RULES));
    }

    @GetMapping("/user-agreement")
    @Operation(summary = "获取最新已发布用户协议")
    public ResponseResult<PrivacyDocViewVO> userAgreement() {
        return ResponseResult.success(docService.getByDocType(DocType.USER_AGREEMENT));
    }
}
