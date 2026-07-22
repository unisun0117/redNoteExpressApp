package cn.qdm.tob.modules.system.dict.service.impl;

import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.modules.system.dict.api.internal.DictApi;
import cn.qdm.tob.modules.system.dict.domain.SysDict;
import cn.qdm.tob.modules.system.dict.mapper.SysDictMapper;
import cn.qdm.tob.modules.system.dict.service.DictItemService;
import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.system.dict.DictMapping;
import cn.qdm.tob.modules.system.dict.api.internal.dto.DictItemDTO;
import cn.qdm.tob.modules.system.dict.api.internal.dto.DictItemDetailDTO;
import cn.qdm.tob.infrastructure.security.util.SecurityUtil;
import cn.qdm.tob.modules.system.dict.domain.SysDictItem;
import cn.qdm.tob.modules.system.dict.dto.DictItemBatchSaveDTO;
import cn.qdm.tob.modules.system.dict.dto.DictItemQuery;
import cn.qdm.tob.modules.system.dict.dto.DictItemSaveDTO;
import cn.qdm.tob.modules.system.dict.mapper.SysDictItemMapper;
import cn.qdm.tob.modules.system.dict.vo.DictItemVO;
import cn.qdm.tob.modules.system.common.CacheKeys;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 字典项 CRUD 服务实现。
 * <p>
 * {@link #load(String)} 以 dictCode 为分组加载全部字典项，
 * 服务于框架层（Jackson 序列化、Excel 导出）；{@link #listItems(String)}
 * 仅返回启用状态的字典项，服务于跨模块调用。
 */
@Service
@RequiredArgsConstructor
public class DictItemServiceImpl extends TobBaseService<SysDictItemMapper, SysDictItem> implements DictItemService, DictApi {

    private final DictMapping dictMapping;
    private final SysDictMapper sysDictMapper;

    // ======================== 委托方法（供 DictServiceImpl 调用） ========================
    @Override
    @Nullable
    @Cacheable(value = CacheKeys.DICT_ITEMS, key = "#group", unless = "#result == null")
    public Map<Object, String> load(String group) {
        return listByDictCode(group)
                .stream()
                .collect(Collectors.toMap(
                        SysDictItem::getValue,
                        SysDictItem::getLabel,
                        (a, _) -> a)
                );
    }


    @Override
    @Cacheable(value = CacheKeys.DICT_ITEMS_ACTIVE, key = "#dictCode")
    public List<DictItemDTO> listItems(String dictCode) {
        if (!isDictActive(dictCode)) return List.of();
        return dictMapping.toItemDTOList(baseMapper.findByDictCodeAndStatus(dictCode, RecordStatus.ACTIVE));
    }


    @Override
    public DictItemDetailDTO getItem(String dictCode, String value) {
        if (!isDictActive(dictCode)) return null;
        SysDictItem entity = lambdaQuery()
                .eq(SysDictItem::getDictCode, dictCode)
                .eq(SysDictItem::getValue, value)
                .one();
        return entity == null ? null : dictMapping.toItemDetailDTO(entity);
    }


    @Override
    public List<DictItemDTO> listItemsByStatus(String dictCode, String status) {
        if (!isDictActive(dictCode)) return List.of();
        RecordStatus recordStatus = RecordStatus.valueOf(status);
        return dictMapping.toItemDTOList(baseMapper.findByDictCodeAndStatus(dictCode, recordStatus));
    }

    // ======================== Controller CRUD ========================


    @Override
    @Cacheable(value = CacheKeys.DICT_ITEMS_ACTIVE, key = "#code")
    public List<DictItemVO> listActiveItemVOs(String code) {
        if (!isDictActive(code)) return List.of();
        return dictMapping.toItemSummaryList(baseMapper.findByDictCodeAndStatus(code, RecordStatus.ACTIVE));
    }


    @Override
    public List<DictItemVO> listItemVOs(DictItemQuery query) {
        return dictMapping.toItemSummaryList(baseMapper.search(query.getCode(), query.getKeyword(), query.getStatus()));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheKeys.DICT_ITEMS, key = "#vo.dictCode"),
            @CacheEvict(value = CacheKeys.DICT_ITEMS_ACTIVE, key = "#vo.dictCode")
    })
    public void updateByDictCodeAndValue(DictItemSaveDTO vo) {
        SysDictItem entity = dictMapping.toItemEntity(vo);
        boolean updated = lambdaUpdate()
                .eq(SysDictItem::getDictCode, vo.getDictCode())
                .eq(SysDictItem::getValue, vo.getValue())
                .update(entity);
        AssertUtils.isTrue(updated, "字典项不存在或已被删除");
    }


    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheKeys.DICT_ITEMS, key = "#dictCode"),
            @CacheEvict(value = CacheKeys.DICT_ITEMS_ACTIVE, key = "#dictCode")
    })
    public void deleteByDictCode(String dictCode) {
        baseMapper.deleteByDictCode(dictCode);
    }


    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheKeys.DICT_ITEMS, key = "#dictCode"),
            @CacheEvict(value = CacheKeys.DICT_ITEMS_ACTIVE, key = "#dictCode")
    })
    public void deleteByDictCodeAndValue(String dictCode, String value) {
        int deleted = baseMapper.deleteByDictCodeAndValue(dictCode, value);
        AssertUtils.isTrue(deleted > 0, "字典项不存在或已被删除");
    }


    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheKeys.DICT_ITEMS, key = "#dictCode"),
            @CacheEvict(value = CacheKeys.DICT_ITEMS_ACTIVE, key = "#dictCode")
    })
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateItems(String dictCode, List<DictItemBatchSaveDTO> items) {
        var currentUser = SecurityUtil.requireCurrentUser();
        Set<String> existingValues = lambdaQuery()
                .eq(SysDictItem::getDictCode, dictCode)
                .list()
                .stream()
                .map(SysDictItem::getValue)
                .collect(Collectors.toSet());

        List<String> duplicates = items.stream()
                .map(DictItemBatchSaveDTO::getValue)
                .filter(existingValues::contains)
                .toList();
        AssertUtils.isTrue(duplicates.isEmpty(), "字典项编码重复: " + String.join(", ", duplicates));

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        List<SysDictItem> entities = dictMapping.toItemEntities(items);
        entities.forEach(item -> {
            item.setDictCode(dictCode);
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            item.setCreatedBy(currentUser.getName());
            item.setUpdatedBy(currentUser.getName());
        });
        // saveBatch 不触发 MetaObjectHandler，审计字段显式设置
        saveBatch(entities);
    }

    // ======================== private helpers ========================

    /** 按字典编码查询全部字典项（含 INACTIVE），用于 load() 展示旧数据 */
    private List<SysDictItem> listByDictCode(String code) {
        return baseMapper.findByDictCodeAndStatus(code, null);
    }

    private boolean isDictActive(String code) {
        SysDict sysDict = sysDictMapper.selectById(code);
        return sysDict != null && RecordStatus.ACTIVE.equals(sysDict.getStatus());
    }
}
