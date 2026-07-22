package cn.qdm.tob.modules.system.privacy.api.admin;

import cn.qdm.tob.framework.excel.annotation.Exportable;
import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.modules.system.privacy.service.FileDownload;
import cn.qdm.tob.modules.system.privacy.service.PrivacyDocService;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocCreationVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocEditVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocExportVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocQueryVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocSummaryVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocViewVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Web 管理端 — 隐私文档管理 API
 */
@RestController
@RequestMapping("/api/admin/privacy/docs")
@RequiredArgsConstructor
public class PrivacyDocAdminController {

    private final PrivacyDocService docService;

    @GetMapping("/list")
    @Operation(summary = "分页查询隐私文档列表")
    @RequirePermission(Permissions.PRIVACY_DOC_VIEW)
    public ResponseResult<IPage<PrivacyDocSummaryVO>> list(@Parameter(description = "查询条件") PrivacyDocQueryVO query) {
        return ResponseResult.success(docService.list(query));
    }

    @GetMapping("/detail")
    @Operation(summary = "查询隐私文档详情")
    @RequirePermission(Permissions.PRIVACY_DOC_VIEW)
    public ResponseResult<PrivacyDocViewVO> detail(@RequestParam Long id) {
        return ResponseResult.success(docService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增隐私文档（multipart：data=JSON, file=附件可选）")
    @RequirePermission(Permissions.PRIVACY_DOC_EDIT)
    public ResponseResult<Long> create(
            @RequestPart("data") @Valid PrivacyDocCreationVO vo,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseResult.success(docService.create(vo, file));
    }

    @PutMapping
    @Operation(summary = "编辑隐私文档（multipart：data=JSON, file=附件可选）")
    @RequirePermission(Permissions.PRIVACY_DOC_EDIT)
    public ResponseResult<?> update(
            @RequestParam Long id,
            @RequestPart("data") @Valid PrivacyDocEditVO vo,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        docService.update(id, vo, file);
        return ResponseResult.success();
    }

    @PutMapping("/publish")
    @Operation(summary = "发布隐私文档")
    @RequirePermission(Permissions.PRIVACY_DOC_EDIT)
    public ResponseResult<?> publish(@RequestParam Long id) {
        docService.publish(id);
        return ResponseResult.success();
    }

    @PutMapping("/withdraw")
    @Operation(summary = "下架隐私文档")
    @RequirePermission(Permissions.PRIVACY_DOC_EDIT)
    public ResponseResult<?> withdraw(@RequestParam Long id) {
        docService.withdraw(id);
        return ResponseResult.success();
    }

    @GetMapping("/export")
    @Operation(summary = "导出隐私文档（排除富文本，请求头加 Action: export）")
    @Exportable(name = "隐私文档", templateType = PrivacyDocExportVO.class, dataPath = "data")
    @RequirePermission(Permissions.PRIVACY_DOC_VIEW)
    public ResponseResult<List<PrivacyDocExportVO>> export(@Parameter(description = "查询条件") PrivacyDocQueryVO query) {
        return ResponseResult.success(docService.exportList(query));
    }

    @GetMapping("/file")
    @Operation(summary = "下载隐私文档附件")
    @RequirePermission(Permissions.PRIVACY_DOC_VIEW)
    public ResponseEntity<Resource> downloadFile(@RequestParam Long id) {
        FileDownload fd = docService.loadFile(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fd.fileName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(fd.resource());
    }
}
