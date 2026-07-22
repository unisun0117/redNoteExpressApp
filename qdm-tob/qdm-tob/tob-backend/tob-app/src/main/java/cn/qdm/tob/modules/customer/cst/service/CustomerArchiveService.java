package cn.qdm.tob.modules.customer.cst.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.infrastructure.security.resolver.CustomerContext;
import cn.qdm.tob.infrastructure.security.resolver.CustomerContextProvider;
import cn.qdm.tob.modules.customer.cst.CustomerArchiveMapping;
import cn.qdm.tob.modules.customer.cst.domain.CstArchiveAuditLog;
import cn.qdm.tob.modules.customer.cst.domain.CstArchiveUserBinding;
import cn.qdm.tob.modules.customer.cst.domain.CstCompanyArchive;
import cn.qdm.tob.modules.customer.cst.dto.CustomerArchiveQuery;
import cn.qdm.tob.modules.customer.cst.mapper.ArchiveAuditLogMapper;
import cn.qdm.tob.modules.customer.cst.mapper.ArchiveUserBindingMapper;
import cn.qdm.tob.modules.customer.cst.mapper.CustomerArchiveMapper;
import cn.qdm.tob.modules.customer.cst.vo.*;
import cn.qdm.tob.modules.customer.operation.salesregion.domain.OprSalesRegion;
import cn.qdm.tob.modules.customer.operation.salesregion.mapper.SalesRegionMapper;
import cn.qdm.tob.modules.customer.operation.salesman.domain.OprSalesman;
import cn.qdm.tob.modules.customer.operation.salesman.mapper.SalesmanMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 客户档案 CRUD 服务
 */
@Service
@RequiredArgsConstructor
public class CustomerArchiveService extends TobBaseService<CustomerArchiveMapper, CstCompanyArchive>
        implements CustomerContextProvider {

    private final CustomerArchiveMapping mapping;
    private final ArchiveAuditLogMapper auditLogMapper;
    private final ArchiveUserBindingMapper userBindingMapper;
    private final SalesmanMapper salesmanMapper;
    private final JdbcTemplate jdbcTemplate;
    private final SalesRegionMapper salesRegionMapper;

    /**
     * 分页查询
     */
    public Page<CustomerArchiveSummaryVO> page(CustomerArchiveQuery query) {
        LambdaQueryWrapper<CstCompanyArchive> wrapper = buildQueryWrapper(query);
        wrapper.orderByDesc(CstCompanyArchive::getCreatedAt);

        Page<CstCompanyArchive> entityPage = baseMapper.selectPage(
            new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        Page<CustomerArchiveSummaryVO> voPage = new Page<>(
            query.getPageNum(), query.getPageSize(), entityPage.getTotal());
        List<CustomerArchiveSummaryVO> summaries = mapping.toSummaryList(entityPage.getRecords());

        // 补充绑定用户数量
        for (CustomerArchiveSummaryVO summary : summaries) {
            Integer count = Math.toIntExact(userBindingMapper.selectCount(
                new LambdaQueryWrapper<CstArchiveUserBinding>()
                    .eq(CstArchiveUserBinding::getArchiveId, summary.getId())
                    .eq(CstArchiveUserBinding::getBindingStatus, "BOUND")));
            summary.setBoundUserCount(count);
        }

        voPage.setRecords(summaries);
        return voPage;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<CstCompanyArchive> buildQueryWrapper(CustomerArchiveQuery query) {
        LambdaQueryWrapper<CstCompanyArchive> wrapper = new LambdaQueryWrapper<>();

        if (query.getCompanyName() != null && !query.getCompanyName().isBlank()) {
            wrapper.like(CstCompanyArchive::getCompanyName, query.getCompanyName());
        }
        if (query.getSalesRegionId() != null) {
            wrapper.eq(CstCompanyArchive::getSalesRegionId, query.getSalesRegionId());
        }
        if (query.getSalesRegionName() != null && !query.getSalesRegionName().isBlank()) {
            wrapper.like(CstCompanyArchive::getSalesRegionName, query.getSalesRegionName());
        }
        if (query.getProvince() != null && !query.getProvince().isBlank()) {
            wrapper.eq(CstCompanyArchive::getProvince, query.getProvince());
        }
        if (query.getCity() != null && !query.getCity().isBlank()) {
            wrapper.eq(CstCompanyArchive::getCity, query.getCity());
        }
        if (query.getDistrict() != null && !query.getDistrict().isBlank()) {
            wrapper.eq(CstCompanyArchive::getDistrict, query.getDistrict());
        }
        if (query.getSalesmanName() != null && !query.getSalesmanName().isBlank()) {
            wrapper.like(CstCompanyArchive::getSalesmanName, query.getSalesmanName());
        }
        if (query.getAuditStatus() != null && !query.getAuditStatus().isBlank()) {
            wrapper.eq(CstCompanyArchive::getAuditStatus, query.getAuditStatus());
        }
        if (query.getAuditorName() != null && !query.getAuditorName().isBlank()) {
            wrapper.like(CstCompanyArchive::getAuditorName, query.getAuditorName());
        }
        if (query.getStartDate() != null && !query.getStartDate().isBlank()) {
            wrapper.ge(CstCompanyArchive::getCreatedAt, query.getStartDate() + " 00:00:00");
        }
        if (query.getEndDate() != null && !query.getEndDate().isBlank()) {
            wrapper.le(CstCompanyArchive::getCreatedAt, query.getEndDate() + " 23:59:59");
        }

        return wrapper;
    }

    /**
     * 按ID查询详情
     */
    public CustomerArchiveViewVO getById(Long id) {
        CstCompanyArchive entity = baseMapper.selectById(id);
        AssertUtils.notNull(entity, "客户档案不存在");

        CustomerArchiveViewVO vo = mapping.toView(entity);

        // 查询审核历史
        List<CstArchiveAuditLog> logs = auditLogMapper.selectList(
            new LambdaQueryWrapper<CstArchiveAuditLog>()
                .eq(CstArchiveAuditLog::getArchiveId, id)
                .orderByDesc(CstArchiveAuditLog::getCreatedAt));
        vo.setAuditLogs(mapping.toAuditLogVOList(logs));

        // 查询已绑定用户
        List<CstArchiveUserBinding> bindings = userBindingMapper.selectList(
            new LambdaQueryWrapper<CstArchiveUserBinding>()
                .eq(CstArchiveUserBinding::getArchiveId, id)
                .eq(CstArchiveUserBinding::getBindingStatus, "BOUND"));
        vo.setBoundUsers(mapping.toBoundUserVOList(bindings));

        return vo;
    }

    /**
     * 后台新增客户档案
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(CustomerArchiveCreateVO vo, String operatorName) {
        CstCompanyArchive entity = mapping.toEntity(vo);

        // 处理经纬度
        if (vo.getLongitude() != null && !vo.getLongitude().isBlank()) {
            entity.setLongitude(new BigDecimal(vo.getLongitude()));
        }
        if (vo.getLatitude() != null && !vo.getLatitude().isBlank()) {
            entity.setLatitude(new BigDecimal(vo.getLatitude()));
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        entity.setAuditStatus("APPROVED");
        entity.setAuditTime(now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setSubmitUserName(operatorName);

        baseMapper.insert(entity);

        // 记录审核日志
        addAuditLog(entity.getId(), "APPROVE", 0L, operatorName, "后台新增（自动通过）");
    }

    /**
     * 编辑业务属性（仅已通过状态可编辑）
     */
    @Transactional(rollbackFor = Exception.class)
    public void edit(Long id, CustomerArchiveEditVO vo, String operatorName) {
        CstCompanyArchive existing = baseMapper.selectById(id);
        AssertUtils.notNull(existing, "客户档案不存在");

        existing.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));

        if (vo.getContactName() != null && !vo.getContactName().isBlank()) {
            existing.setContactName(vo.getContactName());
        }
        if (vo.getContactPhone() != null && !vo.getContactPhone().isBlank()) {
            existing.setContactPhone(vo.getContactPhone());
        }
        if (vo.getPriceGroup() != null) {
            existing.setPriceGroup(vo.getPriceGroup());
        }
        if (vo.getSettleCompany() != null) {
            existing.setSettleCompany(vo.getSettleCompany());
        }
        if (vo.getBusinessType() != null) {
            existing.setBusinessType(vo.getBusinessType());
        }
        if (vo.getSettleType() != null) {
            existing.setSettleType(vo.getSettleType());
        }
        if (vo.getInternalRemark() != null) {
            existing.setInternalRemark(vo.getInternalRemark());
        }

        baseMapper.updateById(existing);
    }

    /**
     * 手动分配审核人（仅待审核状态可分配）
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignAuditor(Long id, AssignAuditorVO vo, Long operatorId, String operatorName) {
        CstCompanyArchive existing = baseMapper.selectById(id);
        AssertUtils.notNull(existing, "客户档案不存在");
        AssertUtils.isTrue("PENDING".equals(existing.getAuditStatus()), "仅待审核状态可分配审核人");

        existing.setAuditorId(vo.getAuditorId());
        existing.setAuditorName(vo.getAuditorName());
        existing.setAuditorType(vo.getAuditorType());
        existing.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));

        baseMapper.updateById(existing);

        addAuditLog(id, "ASSIGN", operatorId != null ? operatorId : 0L, operatorName, "分配审核人：" + vo.getAuditorName());
    }

    /**
     * 绑定小程序用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void bindUser(Long archiveId, Long userId, String userName, String userMobile, String operatorName) {
        CstCompanyArchive archive = baseMapper.selectById(archiveId);
        AssertUtils.notNull(archive, "客户档案不存在");
        AssertUtils.isTrue("APPROVED".equals(archive.getAuditStatus()), "仅已通过状态的档案可绑定用户");

        // 检查是否已绑定
        CstArchiveUserBinding existing = userBindingMapper.selectOne(
            new LambdaQueryWrapper<CstArchiveUserBinding>()
                .eq(CstArchiveUserBinding::getArchiveId, archiveId)
                .eq(CstArchiveUserBinding::getUserId, userId)
                .eq(CstArchiveUserBinding::getBindingStatus, "BOUND"));

        if (existing != null) {
            // 已绑定，无需处理
            return;
        }

        // 检查是否有已解绑的记录
        CstArchiveUserBinding unbound = userBindingMapper.selectOne(
            new LambdaQueryWrapper<CstArchiveUserBinding>()
                .eq(CstArchiveUserBinding::getArchiveId, archiveId)
                .eq(CstArchiveUserBinding::getUserId, userId)
                .eq(CstArchiveUserBinding::getBindingStatus, "UNBOUND"));

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

        if (unbound != null) {
            unbound.setBindingStatus("BOUND");
            unbound.setUpdatedAt(now);
            userBindingMapper.updateById(unbound);
        } else {
            CstArchiveUserBinding binding = new CstArchiveUserBinding();
            binding.setArchiveId(archiveId);
            binding.setUserId(userId);
            binding.setUserName(userName);
            binding.setUserMobile(userMobile);
            binding.setMemberRole("MEMBER");
            binding.setBindingStatus("BOUND");
            binding.setCreatedAt(now);
            binding.setUpdatedAt(now);
            userBindingMapper.insert(binding);
        }
    }

    /**
     * 解绑小程序用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void unbindUser(Long archiveId, Long userId, String operatorName) {
        CstArchiveUserBinding binding = userBindingMapper.selectOne(
            new LambdaQueryWrapper<CstArchiveUserBinding>()
                .eq(CstArchiveUserBinding::getArchiveId, archiveId)
                .eq(CstArchiveUserBinding::getUserId, userId)
                .eq(CstArchiveUserBinding::getBindingStatus, "BOUND"));

        AssertUtils.notNull(binding, "绑定关系不存在");

        binding.setBindingStatus("UNBOUND");
        binding.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        userBindingMapper.updateById(binding);
    }

    /**
     * 查询可绑定的小程序用户（从 sys_user 真实查询）
     */
    public List<CustomerArchiveViewVO.BoundUserVO> searchUsers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>();
        }

        String sql = "SELECT id, COALESCE(NULLIF(real_name,''), NULLIF(mobile,''), '未知') AS real_name, mobile "
                   + "FROM sys_user WHERE mobile LIKE ? OR real_name LIKE ? LIMIT 20";
        String likeKeyword = "%" + keyword + "%";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, likeKeyword, likeKeyword);

        List<CustomerArchiveViewVO.BoundUserVO> users = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            CustomerArchiveViewVO.BoundUserVO user = new CustomerArchiveViewVO.BoundUserVO();
            user.setUserId(((Number) row.get("id")).longValue());
            user.setUserName((String) row.get("real_name"));
            user.setUserMobile((String) row.get("mobile"));
            users.add(user);
        }
        return users;
    }

    /**
     * 根据用户ID查询用户信息（复用 SalesmanService 模式）
     */
    private Map<String, Object> getUserById(Long userId) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT COALESCE(NULLIF(real_name,''), NULLIF(mobile,''), '未知') AS real_name, mobile FROM sys_user WHERE id = ?",
                userId);
        } catch (Exception e) {
            return Map.of("real_name", "未知", "mobile", "");
        }
    }

    /**
     * 添加审核日志
     */
    private void addAuditLog(Long archiveId, String action, Long operatorId, String operatorName, String remark) {
        CstArchiveAuditLog log = new CstArchiveAuditLog();
        log.setArchiveId(archiveId);
        log.setAction(action);
        log.setOperatorId(operatorId != null ? operatorId : 0L);
        log.setOperatorType("ADMIN");
        log.setOperatorName(operatorName);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        auditLogMapper.insert(log);
    }

    /**
     * 按ID查询原始实体（仅返回数据库记录）
     */
    public CstCompanyArchive getEntityById(Long id) {
        return baseMapper.selectById(id);
    }

    // ========================================================================
    //  小程序端 (Mall) 业务方法
    // ========================================================================

    /**
     * 小程序用户查询自己的地址列表（含绑定的地址）
     */
    public Page<CstCompanyArchive> getMallList(Long userId, int pageNum, int pageSize) {
        // 查询该用户提交的地址和他已绑定的地址
        List<CstArchiveUserBinding> bindings = userBindingMapper.selectList(
            new LambdaQueryWrapper<CstArchiveUserBinding>()
                .eq(CstArchiveUserBinding::getUserId, userId)
                .eq(CstArchiveUserBinding::getBindingStatus, "BOUND"));

        List<Long> archiveIds = new ArrayList<>();
        for (CstArchiveUserBinding b : bindings) {
            archiveIds.add(b.getArchiveId());
        }

        LambdaQueryWrapper<CstCompanyArchive> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(CstCompanyArchive::getSubmitUserId, userId)
                .or().in(archiveIds.size() > 0, CstCompanyArchive::getId, archiveIds));
        wrapper.orderByDesc(CstCompanyArchive::getCreatedAt);

        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 小程序用户提交新地址
     */
    @Transactional(rollbackFor = Exception.class)
    public Long submitAddress(CstCompanyArchive entity, Long userId, String userName) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        entity.setSubmitUserId(userId);
        entity.setSubmitUserName(userName);
        entity.setAuditStatus("PENDING");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // 处理推荐码：如果有推荐码，查找对应业务员设为审核人
        if (entity.getReferralCode() != null && !entity.getReferralCode().isBlank()) {
            OprSalesman salesman = salesmanMapper.selectOne(
                new LambdaQueryWrapper<OprSalesman>()
                    .eq(OprSalesman::getReferralCode, entity.getReferralCode())
                    .eq(OprSalesman::getCodeStatus, "VALID"));
            if (salesman != null) {
                entity.setAuditorId(salesman.getId());
                entity.setAuditorType("SALESMAN");
                entity.setSalesmanId(salesman.getId());

                // 从 sys_user 查询业务员姓名和手机号
                Map<String, Object> user = getUserById(salesman.getUserId());
                String salesmanName = (String) user.getOrDefault("real_name", "");
                entity.setAuditorName(salesmanName);
                entity.setSalesmanName(salesmanName);
            }
        }

        baseMapper.insert(entity);

        // 记录审核日志
        addAuditLog(entity.getId(), "SUBMIT", userId, userName, "小程序提交");

        return entity.getId();
    }

    /**
     * 小程序用户重新提交驳回的地址
     */
    @Transactional(rollbackFor = Exception.class)
    public void resubmitAddress(Long id, CstCompanyArchive update, Long userId, String userName) {
        CstCompanyArchive existing = baseMapper.selectById(id);
        AssertUtils.notNull(existing, "地址不存在");
        AssertUtils.isTrue("REJECTED".equals(existing.getAuditStatus()), "仅已驳回状态可重新提交");
        AssertUtils.isTrue(existing.getSubmitUserId().equals(userId), "只能编辑自己提交的地址");

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        existing.setCompanyName(update.getCompanyName());
        existing.setLicenseNo(update.getLicenseNo());
        existing.setDoorPhoto(update.getDoorPhoto());
        existing.setLicensePhoto(update.getLicensePhoto());
        existing.setContactName(update.getContactName());
        existing.setContactPhone(update.getContactPhone());
        existing.setProvince(update.getProvince());
        existing.setCity(update.getCity());
        existing.setDistrict(update.getDistrict());
        existing.setAddress(update.getAddress());
        existing.setLongitude(update.getLongitude());
        existing.setLatitude(update.getLatitude());
        existing.setReceiveTimeStart(update.getReceiveTimeStart());
        existing.setReceiveTimeEnd(update.getReceiveTimeEnd());
        existing.setReceiveRequirement(update.getReceiveRequirement());
        existing.setStoragePhotos(update.getStoragePhotos());
        existing.setAuditStatus("PENDING");
        existing.setAuditRejectReason(null);
        existing.setUpdatedAt(now);

        baseMapper.updateById(existing);

        addAuditLog(id, "RESUBMIT", userId, userName, "驳回后重新提交");
    }

    /**
     * 验证推荐码（模拟数据实现）
     */
    public void verifyReferralCode(String referralCode) {
        // TODO: 调用 SalesmanService 验证推荐码
        AssertUtils.isTrue(referralCode != null && !referralCode.isBlank(), "推荐码不能为空");
        // 暂时只要长度合理就认为有效
        AssertUtils.isTrue(referralCode.length() >= 4, "推荐码无效");
    }

    /**
     * 查询地址成员列表
     */
    public List<CstArchiveUserBinding> getMembers(Long archiveId) {
        return userBindingMapper.selectList(
            new LambdaQueryWrapper<CstArchiveUserBinding>()
                .eq(CstArchiveUserBinding::getArchiveId, archiveId)
                .eq(CstArchiveUserBinding::getBindingStatus, "BOUND"));
    }

    /**
     * 生成邀请码（模拟数据实现）
     */
    public String generateInviteCode(Long archiveId, Long userId) {
        // 验证权限：只有地址管理员可以生成邀请码
        CstArchiveUserBinding adminBinding = userBindingMapper.selectOne(
            new LambdaQueryWrapper<CstArchiveUserBinding>()
                .eq(CstArchiveUserBinding::getArchiveId, archiveId)
                .eq(CstArchiveUserBinding::getUserId, userId)
                .eq(CstArchiveUserBinding::getMemberRole, "ADMIN")
                .eq(CstArchiveUserBinding::getBindingStatus, "BOUND"));

        AssertUtils.notNull(adminBinding, "仅地址管理员可以生成邀请码");

        // 模拟生成8位邀请码
        String inviteCode = String.format("%08d", (int)(Math.random() * 99999999));
        return inviteCode;
    }

    /**
     * 通过邀请码加入地址（模拟数据实现）
     */
    @Transactional(rollbackFor = Exception.class)
    public void joinByInviteCode(String inviteCode, Long userId, String userName, String userMobile) {
        AssertUtils.isTrue(inviteCode != null && !inviteCode.isBlank(), "邀请码不能为空");
        // TODO: 验证邀请码有效性及24小时过期
        // 模拟：取第一个已通过的地址
        CstCompanyArchive archive = baseMapper.selectOne(
            new LambdaQueryWrapper<CstCompanyArchive>()
                .eq(CstCompanyArchive::getAuditStatus, "APPROVED")
                .orderByDesc(CstCompanyArchive::getCreatedAt)
                .last("LIMIT 1"));

        AssertUtils.notNull(archive, "邀请码无效或已过期");

        // 检查是否已绑定
        CstArchiveUserBinding existing = userBindingMapper.selectOne(
            new LambdaQueryWrapper<CstArchiveUserBinding>()
                .eq(CstArchiveUserBinding::getArchiveId, archive.getId())
                .eq(CstArchiveUserBinding::getUserId, userId)
                .eq(CstArchiveUserBinding::getBindingStatus, "BOUND"));

        AssertUtils.isNull(existing, "你已绑定该地址，无需重复加入");

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        CstArchiveUserBinding binding = new CstArchiveUserBinding();
        binding.setArchiveId(archive.getId());
        binding.setUserId(userId);
        binding.setUserName(userName);
        binding.setUserMobile(userMobile);
        binding.setMemberRole("MEMBER");
        binding.setBindingStatus("BOUND");
        binding.setCreatedAt(now);
        binding.setUpdatedAt(now);
        userBindingMapper.insert(binding);
    }

    /**
     * 小程序用户解绑成员
     */
    @Transactional(rollbackFor = Exception.class)
    public void unbindMallMember(Long archiveId, Long targetUserId, Long operatorUserId) {
        // 验证操作者是地址管理员
        CstArchiveUserBinding operatorBinding = userBindingMapper.selectOne(
            new LambdaQueryWrapper<CstArchiveUserBinding>()
                .eq(CstArchiveUserBinding::getArchiveId, archiveId)
                .eq(CstArchiveUserBinding::getUserId, operatorUserId)
                .eq(CstArchiveUserBinding::getMemberRole, "ADMIN")
                .eq(CstArchiveUserBinding::getBindingStatus, "BOUND"));

        AssertUtils.notNull(operatorBinding, "仅地址管理员可以解绑成员");
        AssertUtils.isTrue(!targetUserId.equals(operatorUserId), "地址管理员不可被解绑");

        CstArchiveUserBinding targetBinding = userBindingMapper.selectOne(
            new LambdaQueryWrapper<CstArchiveUserBinding>()
                .eq(CstArchiveUserBinding::getArchiveId, archiveId)
                .eq(CstArchiveUserBinding::getUserId, targetUserId)
                .eq(CstArchiveUserBinding::getBindingStatus, "BOUND"));

        AssertUtils.notNull(targetBinding, "绑定关系不存在");

        targetBinding.setBindingStatus("UNBOUND");
        targetBinding.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        userBindingMapper.updateById(targetBinding);
    }

    // ========================================================================
    //  CustomerContextProvider 实现
    // ========================================================================

    @Override
    public CustomerContext resolve(Long userId, Long addressId) {
        AssertUtils.notNull(addressId, "请选择收货地址");

        // 校验用户绑定
        var binding = userBindingMapper.lambdaSelectOne(w -> w
                        .eq(CstArchiveUserBinding::getUserId, userId)
                        .eq(CstArchiveUserBinding::getArchiveId, addressId)
                        .eq(CstArchiveUserBinding::getBindingStatus, "BOUND"));

        AssertUtils.notNull(binding.isPresent(), "您未绑定该收货地址");

        // 校验地址审核状态
        CstCompanyArchive archive = baseMapper.selectById(addressId);
        AssertUtils.notNull(archive, "客户不存在");
        AssertUtils.isTrue("APPROVED".equals(archive.getAuditStatus()), "客户暂不可用");
        AssertUtils.notNull(archive.getSalesRegionId(), "客户未关联销售大区");

        // 获取销售大区编号
        OprSalesRegion region = salesRegionMapper.selectById(archive.getSalesRegionId());
        AssertUtils.notNull(region, "销售大区无效");
        CustomerContext ctx = mapping.toContext(archive);
        ctx.setSalesRegionCode(region.getCode());
        return ctx;
    }
}
