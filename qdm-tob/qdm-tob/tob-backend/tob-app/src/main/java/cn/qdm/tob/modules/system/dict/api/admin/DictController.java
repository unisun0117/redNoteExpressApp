package cn.qdm.tob.modules.system.dict.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.modules.system.dict.dto.DictPageQuery;
import cn.qdm.tob.modules.system.dict.dto.DictSaveDTO;
import cn.qdm.tob.modules.system.dict.vo.DictVO;
import cn.qdm.tob.modules.system.dict.service.DictService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 字典管理 API
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/api/admin/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @GetMapping("/list")
    @Operation(summary = "获取字典列表（分页搜索）")
    @RequirePermission(Permissions.DICT_VIEW)
    public ResponseResult<IPage<DictVO>> list(@Valid DictPageQuery dto) {
        return ResponseResult.success(dictService.listPage(dto));
    }

    @PostMapping("/create")
    @Operation(summary = "创建字典")
    @RequirePermission(Permissions.DICT_EDIT)
    public ResponseResult<?> create(@Valid @RequestBody DictSaveDTO vo) {
        dictService.create(vo);
        return ResponseResult.success();
    }

    @PostMapping("/update")
    @Operation(summary = "更新字典")
    @RequirePermission(Permissions.DICT_EDIT)
    public ResponseResult<?> update(@Valid @RequestBody DictSaveDTO vo) {
        dictService.update(vo);
        return ResponseResult.success();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除字典")
    @RequirePermission(Permissions.DICT_DELETE)
    public ResponseResult<?> delete(@RequestParam String code) {
        dictService.delete(code);
        return ResponseResult.success();
    }
}
