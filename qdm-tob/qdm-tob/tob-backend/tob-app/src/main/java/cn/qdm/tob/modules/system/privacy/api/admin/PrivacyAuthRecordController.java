package cn.qdm.tob.modules.system.privacy.api.admin;

import cn.qdm.tob.framework.excel.annotation.Exportable;
import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.modules.system.privacy.service.PrivacyAuthRecordService;
import cn.qdm.tob.modules.system.privacy.vo.AuthRecordExportVO;
import cn.qdm.tob.modules.system.privacy.vo.AuthRecordQueryVO;
import cn.qdm.tob.modules.system.privacy.vo.AuthRecordViewVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Web 管理端 — 隐私授权记录 API
 */
@RestController
@RequestMapping("/api/admin/privacy/auth-records")
@RequiredArgsConstructor
public class PrivacyAuthRecordController {

    private final PrivacyAuthRecordService authRecordService;

    @GetMapping("/list")
    @Operation(summary = "分页查询授权记录列表")
    @RequirePermission(Permissions.PRIVACY_DOC_VIEW)
    public ResponseResult<IPage<AuthRecordViewVO>> list(@Parameter(description = "查询条件") AuthRecordQueryVO query) {
        return ResponseResult.success(authRecordService.list(query));
    }

    @GetMapping("/export")
    @Operation(summary = "导出授权记录（请求头加 Action: export）")
    @Exportable(name = "隐私授权记录", templateType = AuthRecordExportVO.class, dataPath = "data")
    @RequirePermission(Permissions.PRIVACY_DOC_VIEW)
    public ResponseResult<List<AuthRecordExportVO>> export(@Parameter(description = "查询条件") AuthRecordQueryVO query) {
        return ResponseResult.success(authRecordService.exportList(query));
    }
}
