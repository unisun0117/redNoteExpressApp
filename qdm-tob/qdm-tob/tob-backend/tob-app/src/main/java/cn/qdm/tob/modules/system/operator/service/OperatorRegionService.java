package cn.qdm.tob.modules.system.operator.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.infrastructure.security.util.SecurityUtil;
import cn.qdm.tob.modules.system.operator.domain.SysOperatorRegion;
import cn.qdm.tob.modules.system.operator.mapper.SysOperatorMapper;
import cn.qdm.tob.modules.system.operator.mapper.SysOperatorRegionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 运营人员-销售大区绑定服务（覆盖式分配）。
 * <p>通过 region_code 关联，不依赖 SalesRegionApi。</p>
 */
@Service
@RequiredArgsConstructor
public class OperatorRegionService extends TobBaseService<SysOperatorRegionMapper, SysOperatorRegion> {

    private final SysOperatorMapper sysOperatorMapper;

    /**
     * 查询运营人员已绑定的销售大区 code 列表
     */
    public List<String> getRegionCodes(Long operatorId) {
        return baseMapper.findByOperatorId(operatorId)
                .stream().map(SysOperatorRegion::getRegionCode).toList();
    }

    /**
     * 增量覆盖式替换运营人员的销售大区绑定。
     * <p>不做大区有效性校验，前端传入什么 code 就保存什么。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void setRegions(Long operatorId, Set<String> regionCodes) {
        AssertUtils.notNull(sysOperatorMapper.selectById(operatorId), "用户不存在");

        // 查现有绑定
        Set<String> existing = baseMapper.findByOperatorId(operatorId).stream()
                .map(SysOperatorRegion::getRegionCode)
                .collect(Collectors.toSet());

        // 计算增量差异
        Set<String> toAdd = new HashSet<>(regionCodes);
        toAdd.removeAll(existing);

        Set<String> toRemove = new HashSet<>(existing);
        toRemove.removeAll(regionCodes);

        // 删除移除的
        if (!toRemove.isEmpty()) {
            baseMapper.deleteByOperatorIdAndRegionCodes(operatorId, new ArrayList<>(toRemove));
        }

        // 批量插入新增的
        if (!toAdd.isEmpty()) {
            String createdBy = SecurityUtil.getCurrentUser().getName();
            LocalDateTime now = LocalDateTime.now();
            List<SysOperatorRegion> list = toAdd.stream()
                    .map(code -> new SysOperatorRegion(operatorId, code, createdBy, now))
                    .toList();
            baseMapper.batchInsert(list);
        }
    }
}
