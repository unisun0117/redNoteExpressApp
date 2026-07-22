package cn.qdm.tob.modules.system.role.service;

import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.system.menu.domain.SysMenu;
import cn.qdm.tob.modules.system.menu.service.SysMenuService;
import cn.qdm.tob.modules.system.role.RoleMapping;
import cn.qdm.tob.modules.system.role.domain.SysRole;
import cn.qdm.tob.modules.system.role.mapper.SysRoleMapper;
import cn.qdm.tob.modules.system.rbac.api.internal.AuthorityApi;
import cn.qdm.tob.modules.system.rbac.service.OperatorRoleService;
import cn.qdm.tob.modules.system.rbac.service.RoleMenuService;
import cn.qdm.tob.modules.system.role.api.internal.dto.RoleSimpleDTO;
import cn.qdm.tob.modules.system.role.vo.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色 CRUD 及角色-菜单绑定服务
 */
@Service
@RequiredArgsConstructor
public class SysRoleService extends TobBaseService<SysRoleMapper, SysRole> {

    private final SysRoleMapper roleMapper;
    private final RoleMenuService roleMenuService;
    private final OperatorRoleService operatorRoleService;
    private final SysMenuService menuService;
    private final AuthorityApi authorityApi;

    /** 分页搜索 */
    public IPage<RoleViewVO> pageSearch(RoleQueryVO query) {
        IPage<SysRole> page = roleMapper.pageSearch(query);
        return page.convert(RoleMapping.INSTANCE::toView);
    }

    /** 查看详情 */
    public RoleViewVO getById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new TobServiceException(ErrorCode.NOT_FOUND.code(), "角色不存在: " + id);
        }
        return RoleMapping.INSTANCE.toView(role);
    }

    /** 查询角色已绑定的菜单 ID */
    public Set<Long> getMenuIds(Long roleId) {
        if (roleMapper.selectById(roleId) == null) {
            throw new TobServiceException(ErrorCode.NOT_FOUND.code(), "角色不存在: " + roleId);
        }
        return roleMenuService.listByRoleId(roleId);
    }

    /** 新增角色 */
    @Transactional
    public Long create(RoleCreationVO vo) {
        // 校验 code 唯一
        if (roleMapper.findByCode(vo.getCode()).isPresent()) {
            throw new TobServiceException(ErrorCode.BAD_REQUEST.code(), "该角色编码已存在");
        }
        // 校验 name 唯一
        if (roleMapper.findByName(vo.getName()).isPresent()) {
            throw new TobServiceException(ErrorCode.BAD_REQUEST.code(), "该角色名称已存在");
        }

        SysRole role = new SysRole();
        role.setCode(vo.getCode());
        role.setName(vo.getName());
        role.setDescription(vo.getDescription());
        role.setStatus(vo.getStatus() != null ? vo.getStatus() : RecordStatus.ACTIVE);
        roleMapper.insert(role);

        // 保存菜单绑定
        if (vo.getMenuIds() != null && !vo.getMenuIds().isEmpty()) {
            setMenus(role.getId(), vo.getMenuIds());
        }

        // 新角色，无用户引用，无需清权限缓存
        return role.getId();
    }

    /** 编辑角色 */
    @Transactional
    public void update(RoleEditVO vo) {
        SysRole existing = roleMapper.selectById(vo.getId());
        if (existing == null) {
            throw new TobServiceException(ErrorCode.NOT_FOUND.code(), "角色不存在: " + vo.getId());
        }
        // 校验 name 唯一（排除自身）
        roleMapper.findByName(vo.getName()).ifPresent(r -> {
            if (!r.getId().equals(vo.getId())) {
                throw new TobServiceException(ErrorCode.BAD_REQUEST.code(), "该角色名称已被其他角色使用");
            }
        });

        existing.setName(vo.getName());
        existing.setDescription(vo.getDescription());
        if (vo.getStatus() != null) {
            existing.setStatus(RecordStatus.valueOf(vo.getStatus()));
        }
        // 清空审计字段，让 strictUpdateFill 自动重新填充
        existing.setUpdatedAt(null);
        existing.setUpdatedBy(null);
        roleMapper.updateById(existing);

        // 全量覆盖菜单绑定
        boolean menuChanged = vo.getMenuIds() != null;
        if (menuChanged) {
            setMenus(vo.getId(), vo.getMenuIds());
        }

        // 菜单绑定变更 → 异步清受影响用户的 user:perms + user:menu_ids
        if (menuChanged) {
            authorityApi.evictRolePerms(vo.getId());
        }
    }

    /** 删除角色 + 级联清理 */
    @Transactional
    public void delete(Long id) {
        if (roleMapper.selectById(id) == null) {
            throw new TobServiceException(ErrorCode.NOT_FOUND.code(), "角色不存在: " + id);
        }
        roleMenuService.deleteByRoleId(id);
        List<Long> userIds = operatorRoleService.deleteByRoleId(id);
        roleMapper.deleteById(id);
        authorityApi.evictRolePermsOnDelete(userIds);
    }

    /** 覆盖式替换角色的菜单集合（缓存由调用方 evictRolePerms 处理） */
    @Transactional
    public void setMenus(Long roleId, Set<Long> menuIds) {
        if (!menuIds.isEmpty()) {
            List<SysMenu> menus = menuService.listByIds(menuIds);
            if (menus.size() != menuIds.size()) {
                Set<Long> existIds = menus.stream().map(SysMenu::getId).collect(Collectors.toSet());
                menuIds.stream()
                        .filter(id -> !existIds.contains(id))
                        .findFirst()
                        .ifPresent(id -> { throw new TobServiceException(ErrorCode.BAD_REQUEST.code(), "菜单不存在: " + id); });
            }
        }
        roleMenuService.setRoleMenus(roleId, menuIds);
    }

    public List<RoleSimpleDTO> listAll() {
        return baseMapper.selectList(null).stream()
                .map(r -> {
                    RoleSimpleDTO dto = new RoleSimpleDTO();
                    dto.setId(r.getId());
                    dto.setName(r.getName());
                    return dto;
                })
                .toList();
    }
}
