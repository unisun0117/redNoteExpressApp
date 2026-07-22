package cn.qdm.tob.modules.customer.operation.announcement.api.mall;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.customer.operation.announcement.service.AnnouncementService;
import cn.qdm.tob.modules.customer.operation.announcement.vo.AnnouncementViewVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端 — 公告 API
 */
@RestController
@RequestMapping("/api/mall/announcements")
@RequiredArgsConstructor
public class AnnouncementMallController {

    private final AnnouncementService announcementService;

    @GetMapping
    @Operation(summary = "获取当前用户所属大区的已启用公告列表")
    public ResponseResult<List<AnnouncementViewVO>> list(
            @RequestParam(required = false) String regionCode) {
        return ResponseResult.success(announcementService.getEnabledForMall(regionCode));
    }
}
