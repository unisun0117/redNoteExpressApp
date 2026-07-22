package cn.qdm.tob.modules.customer.operation.announcement.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.modules.customer.operation.announcement.service.AnnouncementService;
import cn.qdm.tob.modules.customer.operation.announcement.vo.AnnouncementCreationVO;
import cn.qdm.tob.modules.customer.operation.announcement.vo.AnnouncementEditVO;
import cn.qdm.tob.modules.customer.operation.announcement.vo.AnnouncementQueryVO;
import cn.qdm.tob.modules.customer.operation.announcement.vo.AnnouncementViewVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公告管理 API
 */
@Tag(name = "公告管理")
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/list")
    @Operation(summary = "分页查询公告列表")
    @RequirePermission(Permissions.ANNOUNCEMENT_VIEW)
    public ResponseResult<IPage<AnnouncementViewVO>> list(AnnouncementQueryVO query) {
        return ResponseResult.success(announcementService.list(query));
    }

    @PostMapping
    @Operation(summary = "新增公告")
    @RequirePermission(Permissions.ANNOUNCEMENT_EDIT)
    public ResponseResult<?> create(@Valid @RequestBody AnnouncementCreationVO vo) {
        announcementService.create(vo);
        return ResponseResult.success();
    }

    @PutMapping
    @Operation(summary = "编辑公告（内容与启用状态，销售大区不可修改）")
    @RequirePermission(Permissions.ANNOUNCEMENT_EDIT)
    public ResponseResult<?> update(
            @Parameter(description = "公告ID", required = true) @RequestParam(name = "id") Long id,
            @Valid @RequestBody AnnouncementEditVO vo) {
        announcementService.update(id, vo);
        return ResponseResult.success();
    }

    @PutMapping("/toggle")
    @Operation(summary = "启用/停用公告")
    @RequirePermission(Permissions.ANNOUNCEMENT_EDIT)
    public ResponseResult<?> toggle(
            @Parameter(description = "公告ID", required = true) @RequestParam(name = "id") Long id) {
        announcementService.toggle(id);
        return ResponseResult.success();
    }
}
