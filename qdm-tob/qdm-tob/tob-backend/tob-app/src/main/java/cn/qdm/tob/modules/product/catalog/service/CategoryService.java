package cn.qdm.tob.modules.product.catalog.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.product.catalog.CategoryMapping;
import cn.qdm.tob.modules.product.catalog.domain.PrdCategory;
import cn.qdm.tob.modules.product.catalog.mapper.CategoryMapper;
import cn.qdm.tob.modules.product.catalog.dto.CategoryEditVO;
import cn.qdm.tob.modules.product.catalog.vo.CategoryViewVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService extends TobBaseService<CategoryMapper, PrdCategory> {

    private static final Map<Integer, String> LEVEL_MAP = Map.of(
            0, "大分类",
            1, "中分类",
            2, "小分类"
    );

    private final CategoryMapping mapping;

    public Page<CategoryViewVO> page(Integer pageNum, Integer pageSize,
                                     Integer level, String code, String name) {
        LambdaQueryWrapper<PrdCategory> wrapper = new LambdaQueryWrapper<>();
        if (code != null && !code.isBlank()) {
            wrapper.like(PrdCategory::getId, code);
        }
        if (name != null && !name.isBlank()) {
            wrapper.like(PrdCategory::getName, name);
        }
        if (level != null) {
            wrapper.eq(PrdCategory::getLevel, level);
        }
        wrapper.orderByAsc(PrdCategory::getSort).orderByAsc(PrdCategory::getId);

        Page<PrdCategory> entityPage = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 批量获取父级名称
        List<PrdCategory> records = entityPage.getRecords();
        Set<String> parentIds = records.stream()
                .map(PrdCategory::getParentId)
                .filter(pid -> !"0".equals(pid))
                .collect(Collectors.toSet());
        Map<String, String> parentNameMap;
        if (!parentIds.isEmpty()) {
            parentNameMap = baseMapper.selectBatchIds(parentIds).stream()
                    .collect(Collectors.toMap(PrdCategory::getId, PrdCategory::getName));
        } else {
            parentNameMap = Map.of();
        }

        List<CategoryViewVO> voList = records.stream()
                .map(e -> {
                    CategoryViewVO vo = mapping.toView(e);
                    vo.setLevel(e.getLevel());
                    vo.setLevelName(LEVEL_MAP.getOrDefault(e.getLevel(), ""));
                    vo.setParentName(parentNameMap.get(e.getParentId()));
                    return vo;
                })
                .collect(Collectors.toList());

        Page<CategoryViewVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 小程序端顶级分类列表（按排序号升序）
     */
    public List<CategoryViewVO> listTopLevel() {
        List<PrdCategory> entities = baseMapper.selectList(
                new LambdaQueryWrapper<PrdCategory>()
                        .eq(PrdCategory::getLevel, 0)
                        .eq(PrdCategory::getStatus, "ACTIVE")
                        .orderByAsc(PrdCategory::getSort));
        return entities.stream()
                .map(mapping::toView)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAlias(String id, CategoryEditVO vo) {
        PrdCategory existing = baseMapper.selectById(id);
        AssertUtils.notNull(existing, "商品分类不存在");
        LambdaUpdateWrapper<PrdCategory> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PrdCategory::getId, id)
                .set(PrdCategory::getAlias, vo.getAlias());
        baseMapper.update(null, updateWrapper);
    }

}
