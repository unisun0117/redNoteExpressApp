package cn.qdm.tob.modules.system.dict.service.impl;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.system.dict.DictMapping;
import cn.qdm.tob.modules.system.dict.domain.SysDict;
import cn.qdm.tob.modules.system.dict.dto.DictPageQuery;
import cn.qdm.tob.modules.system.dict.dto.DictSaveDTO;
import cn.qdm.tob.modules.system.dict.mapper.SysDictMapper;
import cn.qdm.tob.modules.system.dict.service.DictItemService;
import cn.qdm.tob.modules.system.dict.service.DictService;
import cn.qdm.tob.modules.system.dict.vo.DictVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字典类型 CRUD 服务实现
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl extends TobBaseService<SysDictMapper, SysDict> implements DictService {

    private final DictMapping dictMapping;
    private final DictItemService dictItemService;

    @Override
    public IPage<DictVO> listPage(DictPageQuery dto) {
        Page<SysDict> pageParam = new Page<>(dto.getPage(), dto.getSize());
        Page<SysDict> result = baseMapper.pageSearch(pageParam, dto.getKeyword());
        return result.convert(dictMapping::toSummary);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(DictSaveDTO vo) {
        AssertUtils.isNull(baseMapper.selectById(vo.getCode()), "字典编码已存在");

        SysDict entity = dictMapping.toEntity(vo);
        // 审计字段由 MyBatisMetaObjectHandler 自动填充
        baseMapper.insert(entity);

        if (CollectionUtils.isNotEmpty(vo.getItems())) {
            dictItemService.batchCreateItems(vo.getCode(), vo.getItems());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DictSaveDTO vo) {
        SysDict existing = baseMapper.selectById(vo.getCode());
        AssertUtils.notNull(existing, "字典不存在");

        existing.setName(vo.getName());
        existing.setDescription(vo.getDescription());
        existing.setStatus(vo.getStatus());
        // 清空审计字段，让 strictUpdateFill 重新填充
        existing.setUpdatedAt(null);
        existing.setUpdatedBy(null);
        baseMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String code) {
        dictItemService.deleteByDictCode(code);
        baseMapper.deleteById(code);
    }
}
