package cn.qdm.tob.modules.system.privacy.api.mall;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.system.privacy.service.PrivacyAuthRecordService;
import cn.qdm.tob.modules.system.privacy.vo.AuthRecordCreationVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端 — 隐私授权记录上报 API
 */
@RestController
@RequestMapping("/api/mall/privacy/auth")
@RequiredArgsConstructor
public class PrivacyAuthMallController {

    private final PrivacyAuthRecordService authRecordService;

    @PostMapping
    @Operation(summary = "提交授权记录（相机/相册/隐私政策同意等）")
    public ResponseResult<?> record(@RequestBody @Valid AuthRecordCreationVO vo) {
        authRecordService.record(vo);
        return ResponseResult.success();
    }
}
