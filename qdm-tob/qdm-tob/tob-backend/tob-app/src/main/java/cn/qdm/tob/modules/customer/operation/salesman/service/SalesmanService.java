package cn.qdm.tob.modules.customer.operation.salesman.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.customer.operation.salesman.domain.OprSalesman;
import cn.qdm.tob.modules.customer.operation.salesman.domain.OprSalesmanPerformance;
import cn.qdm.tob.modules.customer.operation.salesman.mapper.SalesmanMapper;
import cn.qdm.tob.modules.customer.operation.salesman.mapper.SalesmanPerformanceMapper;
import cn.qdm.tob.modules.customer.operation.salesman.vo.AddSalesmanVO;
import cn.qdm.tob.modules.customer.operation.salesman.vo.SalesmanPerformanceVO;
import cn.qdm.tob.modules.customer.operation.salesman.vo.SalesmanReferralVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesmanService extends TobBaseService<SalesmanMapper, OprSalesman> {

    private final SalesmanPerformanceMapper performanceMapper;
    private final JdbcTemplate jdbcTemplate;

    /** 推荐码字符集（排除易混淆字符 0/O、1/I/l） */
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final Random RANDOM = new Random();

    // ===== 推荐码列表 =====

    public Page<SalesmanReferralVO> pageReferrals(Integer pageNum, Integer pageSize,
                                                   String name, String phone) {
        // 1. 查业务员
        LambdaQueryWrapper<OprSalesman> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank() || phone != null && !phone.isBlank()) {
            // 需要关联用户表筛选，先查符合条件的 user_id
            List<Long> userIds = filterUserIds(name, phone);
            if (userIds.isEmpty()) {
                Page<SalesmanReferralVO> empty = new Page<>(pageNum, pageSize, 0);
                empty.setRecords(Collections.emptyList());
                return empty;
            }
            wrapper.in(OprSalesman::getUserId, userIds);
        }
        wrapper.orderByDesc(OprSalesman::getUpdatedAt);
        Page<OprSalesman> entityPage = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 2. 批量查用户信息
        List<Long> userIds2 = entityPage.getRecords().stream()
                .map(OprSalesman::getUserId).collect(Collectors.toList());
        Map<Long, Map<String, Object>> userMap = batchGetUsers(userIds2);

        // 3. 组装 VO
        List<SalesmanReferralVO> vos = entityPage.getRecords().stream().map(e -> {
            SalesmanReferralVO vo = new SalesmanReferralVO();
            vo.setId(e.getId());
            vo.setUserId(e.getUserId());
            vo.setReferralCode(e.getReferralCode());
            vo.setCodeStatus(e.getCodeStatus());
            vo.setUpdatedBy(e.getUpdatedBy());
            vo.setUpdatedAt(e.getUpdatedAt());
            Map<String, Object> u = userMap.getOrDefault(e.getUserId(), Collections.emptyMap());
            vo.setName((String) u.getOrDefault("real_name", ""));
            vo.setPhone((String) u.getOrDefault("mobile", ""));
            vo.setCustomerCount(0); // TODO: 销售客户数从 cst_ 表统计，当前版本暂为 0
            return vo;
        }).collect(Collectors.toList());

        Page<SalesmanReferralVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }

    // ===== 添加业务员 =====

    @Transactional(rollbackFor = Exception.class)
    public SalesmanReferralVO add(AddSalesmanVO vo) {
        // 校验是否已存在
        OprSalesman existing = baseMapper.selectOne(
                new LambdaQueryWrapper<OprSalesman>().eq(OprSalesman::getUserId, vo.getUserId()));
        AssertUtils.isNull(existing, "该用户已是业务员");

        OprSalesman entity = new OprSalesman();
        entity.setUserId(vo.getUserId());
        entity.setReferralCode(generateUniqueCode());
        entity.setCodeStatus("VALID");
        entity.setCreatedBy(vo.getCreatedBy());
        entity.setUpdatedBy(vo.getCreatedBy());
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        baseMapper.insert(entity);

        // 返回 VO
        SalesmanReferralVO vo2 = new SalesmanReferralVO();
        vo2.setId(entity.getId());
        vo2.setUserId(entity.getUserId());
        vo2.setReferralCode(entity.getReferralCode());
        vo2.setCodeStatus(entity.getCodeStatus());
        vo2.setCustomerCount(0);
        vo2.setUpdatedBy(entity.getUpdatedBy());
        vo2.setUpdatedAt(entity.getUpdatedAt());
        // 查用户信息
        Map<String, Object> u = getUser(entity.getUserId());
        vo2.setName((String) u.getOrDefault("real_name", ""));
        vo2.setPhone((String) u.getOrDefault("mobile", ""));
        return vo2;
    }

    // ===== 推荐码操作 =====

    @Transactional(rollbackFor = Exception.class)
    public void regenerateCode(Long id, String updatedBy) {
        OprSalesman entity = baseMapper.selectById(id);
        AssertUtils.notNull(entity, "业务员不存在");
        entity.setReferralCode(generateUniqueCode());
        entity.setCodeStatus("VALID");
        entity.setUpdatedBy(updatedBy);
        entity.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        baseMapper.updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearCode(Long id, String updatedBy) {
        OprSalesman entity = baseMapper.selectById(id);
        AssertUtils.notNull(entity, "业务员不存在");
        entity.setReferralCode(null);
        entity.setCodeStatus("EMPTY");
        entity.setUpdatedBy(updatedBy);
        entity.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        baseMapper.updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCode(Long id, String updatedBy) {
        OprSalesman entity = baseMapper.selectById(id);
        AssertUtils.notNull(entity, "业务员不存在");
        entity.setCodeStatus("INVALID");
        entity.setUpdatedBy(updatedBy);
        entity.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        baseMapper.updateById(entity);
    }

    // ===== 绩效 CRUD =====

    public Page<SalesmanPerformanceVO> pagePerformances(Integer pageNum, Integer pageSize,
                                                         Long salesmanId, String month) {
        LambdaQueryWrapper<OprSalesmanPerformance> wrapper = new LambdaQueryWrapper<>();
        if (salesmanId != null) {
            wrapper.eq(OprSalesmanPerformance::getSalesmanId, salesmanId);
        }
        if (month != null && !month.isBlank()) {
            wrapper.like(OprSalesmanPerformance::getMonth, month);
        }
        wrapper.orderByDesc(OprSalesmanPerformance::getMonth);

        Page<OprSalesmanPerformance> entityPage =
                performanceMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<SalesmanPerformanceVO> vos = entityPage.getRecords().stream().map(e -> {
            SalesmanPerformanceVO vo = new SalesmanPerformanceVO();
            vo.setId(String.valueOf(e.getId()));
            vo.setSalesmanId(String.valueOf(e.getSalesmanId()));
            vo.setSalesmanName(e.getSalesmanName());
            vo.setMonth(e.getMonth());
            vo.setOrderCount(e.getOrderCount());
            vo.setCustomerCount(e.getCustomerCount());
            vo.setSalesAmount(e.getSalesAmount());
            vo.setUpdatedBy(e.getUpdatedBy());
            vo.setUpdatedAt(e.getUpdatedAt());
            return vo;
        }).collect(Collectors.toList());

        Page<SalesmanPerformanceVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addPerformance(SalesmanPerformanceVO vo, String operator) {
        // 新增时校验唯一性
        LambdaQueryWrapper<OprSalesmanPerformance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OprSalesmanPerformance::getSalesmanId, Long.valueOf(vo.getSalesmanId()))
                .eq(OprSalesmanPerformance::getMonth, vo.getMonth());
        AssertUtils.isNull(performanceMapper.selectOne(wrapper), "该业务员该月度的绩效已存在");

        OprSalesmanPerformance entity = new OprSalesmanPerformance();
        entity.setSalesmanId(Long.valueOf(vo.getSalesmanId()));
        entity.setSalesmanName(vo.getSalesmanName());
        entity.setMonth(vo.getMonth());
        entity.setOrderCount(vo.getOrderCount());
        entity.setCustomerCount(vo.getCustomerCount());
        entity.setSalesAmount(vo.getSalesAmount());
        entity.setCreatedBy(operator);
        entity.setUpdatedBy(operator);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        performanceMapper.insert(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void editPerformance(SalesmanPerformanceVO vo, String operator) {
        OprSalesmanPerformance existing = performanceMapper.selectById(Long.valueOf(vo.getId()));
        AssertUtils.notNull(existing, "绩效记录不存在");

        existing.setSalesmanName(vo.getSalesmanName());
        existing.setOrderCount(vo.getOrderCount());
        existing.setCustomerCount(vo.getCustomerCount());
        existing.setSalesAmount(vo.getSalesAmount());
        existing.setUpdatedBy(operator);
        existing.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        performanceMapper.updateById(existing);
    }

    /** 获取全部业务员列表（下拉选项用） */
    public List<SalesmanReferralVO> listAll() {
        List<OprSalesman> entities = baseMapper.selectList(null);
        if (entities.isEmpty()) return Collections.emptyList();

        List<Long> userIds = entities.stream().map(OprSalesman::getUserId).collect(Collectors.toList());
        Map<Long, Map<String, Object>> userMap = batchGetUsers(userIds);

        return entities.stream().map(e -> {
            SalesmanReferralVO vo = new SalesmanReferralVO();
            vo.setId(e.getId());
            vo.setUserId(e.getUserId());
            Map<String, Object> u = userMap.getOrDefault(e.getUserId(), Collections.emptyMap());
            vo.setName((String) u.getOrDefault("real_name", ""));
            vo.setPhone((String) u.getOrDefault("mobile", ""));
            return vo;
        }).collect(Collectors.toList());
    }

    // ===== 用户查询辅助（同一表前缀 sys_，不违反规则） =====

    private List<Long> filterUserIds(String name, String phone) {
        StringBuilder sql = new StringBuilder("SELECT id FROM sys_user WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (name != null && !name.isBlank()) {
            sql.append(" AND real_name LIKE ?");
            params.add("%" + name + "%");
        }
        if (phone != null && !phone.isBlank()) {
            sql.append(" AND mobile LIKE ?");
            params.add("%" + phone + "%");
        }
        return jdbcTemplate.queryForList(sql.toString(), params.toArray(), Long.class);
    }

    private Map<Long, Map<String, Object>> batchGetUsers(List<Long> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();
        String inClause = userIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = "SELECT id, COALESCE(NULLIF(real_name,''), NULLIF(mobile,''), '未知') AS real_name, mobile FROM sys_user WHERE id IN (" + inClause + ")";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return rows.stream().collect(Collectors.toMap(
                r -> ((Number) r.get("id")).longValue(),
                r -> r
        ));
    }

    private Map<String, Object> getUser(Long userId) {
        try {
            return jdbcTemplate.queryForMap(
                    "SELECT COALESCE(NULLIF(real_name,''), NULLIF(mobile,''), '未知') AS real_name, mobile FROM sys_user WHERE id = ?", userId);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private Map<Long, String> batchGetSalesmanNames(List<Long> salesmanIds) {
        if (salesmanIds.isEmpty()) return Collections.emptyMap();
        // 先批量查出 salesman → userId 映射
        Map<Long, Long> sidToUid = new LinkedHashMap<>();
        for (Long sid : salesmanIds) {
            OprSalesman s = baseMapper.selectById(sid);
            if (s != null) sidToUid.put(sid, s.getUserId());
        }
        if (sidToUid.isEmpty()) return Collections.emptyMap();

        // 批量查用户名称（COALESCE 多字段兜底）
        String inClause = sidToUid.values().stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = "SELECT id, COALESCE(NULLIF(real_name,''), NULLIF(mobile,''), '未知') AS name FROM sys_user WHERE id IN (" + inClause + ")";
        Map<Long, String> uidToName = new LinkedHashMap<>();
        jdbcTemplate.queryForList(sql).forEach(row ->
            uidToName.put(((Number) row.get("id")).longValue(), (String) row.get("name"))
        );

        Map<Long, String> result = new LinkedHashMap<>();
        sidToUid.forEach((sid, uid) -> result.put(sid, uidToName.getOrDefault(uid, "未知")));
        // 已删除的销售员也填充默认值
        for (Long sid : salesmanIds) {
            result.putIfAbsent(sid, "未知");
        }
        return result;
    }

    // ===== 推荐码生成 =====

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 3; attempt++) {
            String code = generateCode();
            OprSalesman existing = baseMapper.selectOne(
                    new LambdaQueryWrapper<OprSalesman>()
                            .eq(OprSalesman::getReferralCode, code)
                            .eq(OprSalesman::getCodeStatus, "VALID"));
            if (existing == null) return code;
        }
        throw new RuntimeException("推荐码生成失败，请重试");
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
