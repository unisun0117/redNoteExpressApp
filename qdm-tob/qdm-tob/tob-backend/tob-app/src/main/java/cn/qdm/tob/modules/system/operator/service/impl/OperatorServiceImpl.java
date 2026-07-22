package cn.qdm.tob.modules.system.operator.service.impl;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.system.operator.OperatorMapping;
import cn.qdm.tob.modules.system.operator.enums.OperatorStatus;
import cn.qdm.tob.modules.system.operator.domain.SysOperator;
import cn.qdm.tob.modules.system.operator.dto.OperatorPageQuery;
import cn.qdm.tob.modules.system.operator.dto.OperatorSaveDTO;
import cn.qdm.tob.modules.system.operator.mapper.SysOperatorMapper;
import cn.qdm.tob.modules.system.operator.service.OperatorService;
import cn.qdm.tob.modules.system.operator.service.OperatorStatusFilterService;
import cn.qdm.tob.modules.system.operator.vo.OperatorVO;
import cn.qdm.tob.modules.system.sequence.api.internal.SequenceApi;
import cn.qdm.tob.modules.system.sequence.api.internal.dto.SequenceResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 运营人员 CRUD 服务实现
 */
@Service
@RequiredArgsConstructor
public class OperatorServiceImpl extends TobBaseService<SysOperatorMapper, SysOperator> implements OperatorService {

    private final OperatorMapping operatorMapping;
    private final OperatorStatusFilterService statusFilterService;
    private final SequenceApi sequenceApi;

    /** 手机号脱敏 */
    public static String maskMobile(String mobile) {
        if (mobile == null || mobile.isEmpty()) return "";
        return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    @Override
    public IPage<OperatorVO> listPage(OperatorPageQuery dto) {
        Page<SysOperator> pageParam = new Page<>(
                dto.getPage() != null ? dto.getPage() : 1,
                dto.getSize() != null ? dto.getSize() : 10);
        Page<SysOperator> result = baseMapper.pageSearch(pageParam,
                dto.getEmployeeCode(), dto.getRealName(), dto.getMobile(),
                dto.getStatus(), dto.getType());
        return result.convert(entity -> {
            OperatorVO vo = operatorMapping.toSummary(entity);
            vo.setMobile(maskMobile(entity.getMobile()));
            return vo;
        });
    }

    @Override
    public OperatorVO get(Long id) {
        SysOperator entity = baseMapper.selectById(id);
        AssertUtils.notNull(entity, "用户不存在");
        // 详情接口不脱敏，供编辑弹窗使用
        return operatorMapping.toSummary(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(OperatorSaveDTO dto) {
        AssertUtils.isNull(baseMapper.findByEmployeeCode(dto.getEmployeeCode()).orElse(null),
                "工号已存在: " + dto.getEmployeeCode());
        AssertUtils.isNull(baseMapper.findByMobile(dto.getMobile()).orElse(null),
                "手机号已被其他用户使用");

        SysOperator entity = operatorMapping.toEntity(dto);
        if (entity.getStatus() == null) entity.setStatus(OperatorStatus.ACTIVE);

        // 自动生成推荐码
        SequenceResult seq = sequenceApi.next("USER_RECOMMEND_CODE");
        entity.setRecommendCode(seq.getFormattedValue());

        // 审计字段由 MyBatisMetaObjectHandler 自动填充
        baseMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, OperatorSaveDTO dto) {
        SysOperator existing = baseMapper.selectById(id);
        AssertUtils.notNull(existing, "用户不存在");

        if (dto.getMobile() != null) {
            Optional<SysOperator> byMobile = baseMapper.findByMobile(dto.getMobile());
            AssertUtils.isTrue(byMobile.isEmpty() || byMobile.get().getId().equals(id),
                    "手机号已被其他用户使用");
            existing.setMobile(dto.getMobile());
        }

        if (dto.getRealName() != null) existing.setRealName(dto.getRealName());
        if (dto.getType() != null) existing.setType(dto.getType());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());

        // 清空审计字段，让 strictUpdateFill 自动重新填充
        existing.setUpdatedAt(null);
        existing.setUpdatedBy(null);
        baseMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        SysOperator existing = baseMapper.selectById(id);
        AssertUtils.notNull(existing, "用户不存在");

        OperatorStatus newStatus = OperatorStatus.valueOf(status);
        existing.setStatus(newStatus);

        // 清空审计字段，让 strictUpdateFill 自动重新填充
        existing.setUpdatedAt(null);
        existing.setUpdatedBy(null);
        baseMapper.updateById(existing);

        if (newStatus == OperatorStatus.INACTIVE || newStatus == OperatorStatus.LOCKED) {
            statusFilterService.addToBlacklist(id);
        } else {
            statusFilterService.removeFromBlacklist(id);
        }
    }
}
