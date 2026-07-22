package cn.qdm.tob.modules.system.rbac.service;

import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.infrastructure.security.util.SecurityUtil;
import cn.qdm.tob.modules.system.common.CacheKeys;
import cn.qdm.tob.modules.system.rbac.domain.SysOperatorRole;
import cn.qdm.tob.modules.system.rbac.mapper.SysOperatorRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员-角色绑定服务（覆盖式分配）
 */
@Service
@RequiredArgsConstructor
public class OperatorRoleService extends TobBaseService<SysOperatorRoleMapper, SysOperatorRole> {

    /** 查询管理员已绑定的角色 ID */
    public List<Long> getRoleIds(Long operatorId) {
        return baseMapper.findByOperatorId(operatorId)
                .stream()
                .map(SysOperatorRole::getRoleId)
                .toList();
    }

    /** 增量覆盖式替换管理员的角色集合，同时清 user:perms + user:menu_ids */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheKeys.USER_PERMS, key = "#operatorId"),
            @CacheEvict(value = CacheKeys.USER_MENU_IDS, key = "#operatorId")
    })
    public void setRoles(Long operatorId, Set<Long> roleIds) {
        // 查现有角色
        Set<Long> existing = baseMapper.findByOperatorId(operatorId).stream()
                .map(SysOperatorRole::getRoleId)
                .collect(Collectors.toSet());

        // 计算增量差异
        Set<Long> toAdd = new HashSet<>(roleIds);
        toAdd.removeAll(existing);

        Set<Long> toRemove = new HashSet<>(existing);
        toRemove.removeAll(roleIds);

        // 删除移除的
        if (!toRemove.isEmpty()) {
            baseMapper.deleteByOperatorIdAndRoleIds(operatorId, new ArrayList<>(toRemove));
        }

        // 批量插入新增的
        if (!toAdd.isEmpty()) {
            String createdBy = SecurityUtil.getCurrentUser().getName();
            LocalDateTime now = LocalDateTime.now();
            List<SysOperatorRole> list = toAdd.stream()
                    .map(roleId -> new SysOperatorRole(operatorId, roleId, createdBy, now))
                    .toList();
            baseMapper.batchInsert(list);
        }
    }

    /** 删除角色的所有用户绑定，返回删除前的 userId 列表（角色删除时用） */
    public List<Long> deleteByRoleId(Long roleId) {
        List<Long> userIds = baseMapper.findOperatorIdsByRoleId(roleId);
        baseMapper.deleteByRoleId(roleId);
        return userIds;
    }

    // ==================== 查询方法（供 OperatorAuthorityLoader 用） ====================

    /** 反查某角色下的所有用户 ID */
    public List<Long> findOperatorIdsByRoleId(Long roleId) {
        return baseMapper.findOperatorIdsByRoleId(roleId);
    }

    /** 查用户的所有权限码（一条 SQL JOIN） */
    public Set<String> findPermissionsByOperatorId(Long operatorId, String status) {
        return baseMapper.findPermissionsByOperatorId(operatorId, status);
    }

    /** 查用户的所有 menuId（一条 SQL JOIN，跨所有 group） */
    public Set<Long> findMenuIdsByOperatorId(Long operatorId, String status) {
        return baseMapper.findMenuIdsByOperatorId(operatorId, status);
    }
}