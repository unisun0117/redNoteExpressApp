package cn.qdm.tob.modules.system.rbac.service;

import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.infrastructure.security.util.SecurityUtil;
import cn.qdm.tob.modules.system.rbac.domain.SysMenuPermission;
import cn.qdm.tob.modules.system.rbac.mapper.SysMenuPermissionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 按钮-权限码关联表服务
 */
@Service
public class MenuPermissionService extends TobBaseService<SysMenuPermissionMapper, SysMenuPermission> {

    /** 批量查询多个菜单绑定的权限码（按 menuId 分组） */
    public Map<Long, List<String>> findCodesByMenuIdsGrouped(Set<Long> menuIds) {
        return baseMapper.findCodesByMenuIdsGrouped(menuIds);
    }

    /** 增量覆盖菜单的权限码绑定 */
    public void savePermissionCodes(Long menuId, List<String> codes) {
        // 查现有权限码
        List<SysMenuPermission> existingList = baseMapper.lambdaSelect(w -> w.eq(SysMenuPermission::getMenuId, menuId));
        Set<String> existing = existingList.stream()
                .map(SysMenuPermission::getPermissionCode)
                .collect(Collectors.toSet());

        Set<String> newCodes = codes != null ? new HashSet<>(codes) : Collections.emptySet();

        // 计算增量差异
        Set<String> toAdd = new HashSet<>(newCodes);
        toAdd.removeAll(existing);

        Set<String> toRemove = new HashSet<>(existing);
        toRemove.removeAll(newCodes);

        // 删除移除的
        if (!toRemove.isEmpty()) {
            baseMapper.deleteByMenuIdAndCodes(menuId, new ArrayList<>(toRemove));
        }

        // 批量插入新增的
        if (!toAdd.isEmpty()) {
            String createdBy = SecurityUtil.getCurrentUser().getName();
            LocalDateTime now = LocalDateTime.now();
            List<SysMenuPermission> list = toAdd.stream()
                    .map(code -> new SysMenuPermission(menuId, code, createdBy, now))
                    .toList();
            baseMapper.batchInsert(list);
        }
    }

    /** 删除菜单的所有权限码绑定 */
    public void deleteByMenuId(Long menuId) {
        baseMapper.deleteByMenuId(menuId);
    }
}
