package cn.qdm.tob.modules.system.user.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.system.user.SysUserMapping;
import cn.qdm.tob.modules.system.user.api.internal.SysUserApi;
import cn.qdm.tob.modules.system.user.api.internal.dto.SysUserDto;
import cn.qdm.tob.modules.system.user.domain.SysUser;
import cn.qdm.tob.modules.system.user.enums.UserSource;
import cn.qdm.tob.modules.system.user.enums.UserStatus;
import cn.qdm.tob.modules.system.user.mapper.SysUserMapper;
import cn.qdm.tob.modules.system.user.vo.SysUserCreationVO;
import cn.qdm.tob.modules.system.user.vo.SysUserEditVO;
import cn.qdm.tob.modules.system.user.vo.SysUserSummaryVO;
import cn.qdm.tob.modules.system.user.vo.SysUserViewVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * 用户子域 Service（遵循 RULES.md Modulith：同模块内子域间通过 Service 方法调用，
 * 不直接访问对方 Mapper）。auth 子域的注册/登录逻辑通过本 Service 操作 sys_user。
 * <p>
 * 本轮新增后台客户账号管理 CRUD（page/create/edit/toggleStatus/delete）。
 */
@Service
@RequiredArgsConstructor
public class SysUserService extends TobBaseService<SysUserMapper, SysUser> implements SysUserApi {

    private final SysUserMapping mapping;

    public Optional<SysUser> findByMobile(String mobile) {
        return baseMapper.findByMobile(mobile);
    }

    public Optional<SysUser> findByWechatOpenid(String openid) {
        return baseMapper.findByWechatOpenid(openid);
    }

    /**
     * 新增用户（auth 注册流使用，保留原语义）
     */
    public void insert(SysUser user) {
        baseMapper.insert(user);
    }

    /**
     * 分页查询客户账号
     *
     * @param keyword 姓名或手机号模糊匹配（选填）
     */
    public Page<SysUserSummaryVO> page(Integer pageNum, Integer pageSize,
                                       String keyword, UserSource source, UserStatus status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysUser::getRealName, kw)
                    .or()
                    .like(SysUser::getMobile, kw));
        }
        if (source != null) {
            wrapper.eq(SysUser::getSource, source);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getRegisteredAt);

        Page<SysUser> entityPage = baseMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);

        Page<SysUserSummaryVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(mapping.toSummaryList(entityPage.getRecords()));
        return voPage;
    }

    /**
     * 查询详情
     */
    public SysUserViewVO getDetail(Long id) {
        SysUser entity = baseMapper.selectById(id);
        AssertUtils.notNull(entity, "客户账号不存在");
        return mapping.toView(entity);
    }

    /**
     * 后台新增客户账号（免短信验证码：source=ADMIN、status=ACTIVE、wechat_openid=NULL）
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(SysUserCreationVO vo) {
        // mobile 唯一性校验
        Optional<SysUser> exists = baseMapper.findByMobile(vo.getMobile());
        AssertUtils.isTrue(exists.isEmpty(), "该手机号已被注册");

        SysUser entity = mapping.toEntity(vo);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        entity.setStatus(UserStatus.ACTIVE);
        entity.setSource(UserSource.ADMIN);
        entity.setRegisteredAt(now);
        entity.setUpdatedAt(now);
        baseMapper.insert(entity);
    }

    /**
     * 编辑客户账号（仅姓名可改，手机号不可改）
     */
    @Transactional(rollbackFor = Exception.class)
    public void edit(SysUserEditVO vo) {
        SysUser existing = baseMapper.selectById(vo.getId());
        AssertUtils.notNull(existing, "客户账号不存在");
        existing.setRealName(vo.getRealName().trim());
        existing.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        baseMapper.updateById(existing);
    }

    /**
     * 切换账号状态：ACTIVE ↔ INACTIVE；FROZEN 视为已禁用，启用→ACTIVE
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id) {
        SysUser existing = baseMapper.selectById(id);
        AssertUtils.notNull(existing, "客户账号不存在");
        UserStatus next = UserStatus.ACTIVE.equals(existing.getStatus())
                ? UserStatus.INACTIVE
                : UserStatus.ACTIVE;
        existing.setStatus(next);
        existing.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        baseMapper.updateById(existing);
    }

    /**
     * 硬删除客户账号（释放 mobile 唯一键）
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysUser existing = baseMapper.selectById(id);
        AssertUtils.notNull(existing, "客户账号不存在");
        baseMapper.deleteById(id);
    }

    @Override
    public SysUserDto getUserById(Long id) {
        SysUser user = baseMapper.selectById(id);
        return mapping.toDto(user);
    }
}
