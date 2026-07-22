package cn.qdm.tob.modules.system.dict.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.modules.system.dict.service.DictItemService;
import cn.qdm.tob.modules.system.dict.dto.DictItemBatchSaveDTO;
import cn.qdm.tob.modules.system.dict.dto.DictItemQuery;
import cn.qdm.tob.modules.system.dict.dto.DictItemSaveDTO;
import cn.qdm.tob.modules.system.dict.vo.DictItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典项管理 API
 */
@Tag(name = "字典项管理")
@RestController
@RequestMapping("/api/admin/dict/item")
@RequiredArgsConstructor
public class DictItemController {

    private final DictItemService dictItemService;

    @GetMapping("/list")
    @Operation(summary = "获取字典项列表（支持关键词搜索和状态筛选）")
    @RequirePermission(Permissions.DICT_VIEW)
    public ResponseResult<List<DictItemVO>> list(@Valid DictItemQuery query) {
        return ResponseResult.success(dictItemService.listItemVOs(query));
    }

    @GetMapping("/active")
    @Operation(summary = "获取启用状态的字典项（下拉框用，走缓存）")
    @RequirePermission(Permissions.DICT_VIEW)
    public ResponseResult<List<DictItemVO>> activeItems(@RequestParam String code) {
        return ResponseResult.success(dictItemService.listActiveItemVOs(code));
    }

    @PostMapping("/update")
    @Operation(summary = "更新字典项")
    @RequirePermission(Permissions.DICT_EDIT)
    public ResponseResult<?> update(@Valid @RequestBody DictItemSaveDTO vo) {
        dictItemService.updateByDictCodeAndValue(vo);
        return ResponseResult.success();
    }

    @PostMapping("/batch/create")
    @Operation(summary = "批量新增字典项")
    @RequirePermission(Permissions.DICT_EDIT)
    public ResponseResult<?> batchCreate(@RequestParam String dictCode,
                                          @Valid @RequestBody List<DictItemBatchSaveDTO> items) {
        AssertUtils.notEmpty(items, "字典项不能为空");
        dictItemService.batchCreateItems(dictCode, items);
        return ResponseResult.success();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除字典项")
    @RequirePermission(Permissions.DICT_DELETE)
    public ResponseResult<?> delete(@RequestParam String dictCode,
                                    @RequestParam String value) {
        dictItemService.deleteByDictCodeAndValue(dictCode, value);
        return ResponseResult.success();
    }
}
