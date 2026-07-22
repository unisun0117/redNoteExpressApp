package cn.qdm.tob.modules.system.privacy.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.infrastructure.security.util.SecurityUtil;
import cn.qdm.tob.modules.system.privacy.PrivacyMapping;
import cn.qdm.tob.modules.system.privacy.domain.SysPrivacyAuthRecord;
import cn.qdm.tob.modules.system.privacy.enums.AuthType;
import cn.qdm.tob.modules.system.privacy.mapper.SysPrivacyAuthRecordMapper;
import cn.qdm.tob.modules.system.privacy.vo.AuthRecordCreationVO;
import cn.qdm.tob.modules.system.privacy.vo.AuthRecordExportVO;
import cn.qdm.tob.modules.system.privacy.vo.AuthRecordQueryVO;
import cn.qdm.tob.modules.system.privacy.vo.AuthRecordViewVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 隐私授权记录服务（查询 + 导出 + 小程序上报）。
 */
@Service
@RequiredArgsConstructor
public class PrivacyAuthRecordService extends TobBaseService<SysPrivacyAuthRecordMapper, SysPrivacyAuthRecord> {

    private final SysPrivacyAuthRecordMapper authRecordMapper;
    private final PrivacyMapping mapping;

    /**
     * 小程序端提交授权记录。
     * <p>userId / phone 从当前登录用户的 JWT 中提取，无需前端传入。</p>
     */
    public void record(AuthRecordCreationVO vo) {
        var user = SecurityUtil.requireCurrentUser();
        SysPrivacyAuthRecord entity = new SysPrivacyAuthRecord();
        entity.setOpenid(String.valueOf(user.getUserId()));
        entity.setPhone(user.getPhone());
        entity.setAuthType(vo.getAuthType());
        entity.setVersion(vo.getVersion());
        entity.setAuthTime(LocalDateTime.now());
        authRecordMapper.insert(entity);
    }

    /** 分页查询授权记录 */
    public IPage<AuthRecordViewVO> list(AuthRecordQueryVO query) {
        Page<SysPrivacyAuthRecord> entityPage = authRecordMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), buildWrapper(query));

        Page<AuthRecordViewVO> voPage = new Page<>(
                entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(mapping.toAuthRecordViewVOList(entityPage.getRecords()));
        return voPage;
    }

    /** 导出授权记录（上限 10000 条） */
    public List<AuthRecordExportVO> exportList(AuthRecordQueryVO query) {
        List<SysPrivacyAuthRecord> entities = authRecordMapper.selectList(new Page<>(1, 10000), buildWrapper(query));
        return mapping.toAuthRecordExportVOList(entities);
    }

    private LambdaQueryWrapper<SysPrivacyAuthRecord> buildWrapper(AuthRecordQueryVO query) {
        LambdaQueryWrapper<SysPrivacyAuthRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getOpenid())) {
            wrapper.like(SysPrivacyAuthRecord::getOpenid, query.getOpenid());
        }
        if (StringUtils.isNotBlank(query.getPhone())) {
            wrapper.like(SysPrivacyAuthRecord::getPhone, query.getPhone());
        }
        if (StringUtils.isNotBlank(query.getAuthType())) {
            AuthType authType = EnumUtils.getEnum(AuthType.class, query.getAuthType());
            AssertUtils.notNull(authType, "无效的授权类型: " + query.getAuthType());
            wrapper.eq(SysPrivacyAuthRecord::getAuthType, authType);
        }
        wrapper.orderByDesc(SysPrivacyAuthRecord::getAuthTime);
        return wrapper;
    }
}
