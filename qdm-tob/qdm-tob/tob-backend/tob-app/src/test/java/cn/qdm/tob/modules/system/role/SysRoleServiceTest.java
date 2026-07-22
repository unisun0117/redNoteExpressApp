package cn.qdm.tob.modules.system.role;

import cn.qdm.tob.modules.system.rbac.api.internal.AuthorityApi;
import cn.qdm.tob.modules.system.rbac.service.OperatorRoleService;
import cn.qdm.tob.modules.system.rbac.service.RoleMenuService;
import cn.qdm.tob.modules.system.menu.service.SysMenuService;
import cn.qdm.tob.modules.system.role.api.internal.dto.RoleSimpleDTO;
import cn.qdm.tob.modules.system.role.domain.SysRole;
import cn.qdm.tob.modules.system.role.mapper.SysRoleMapper;
import cn.qdm.tob.modules.system.role.service.SysRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("角色 Service")
class SysRoleServiceTest {

    @Mock
    private SysRoleMapper roleMapper;

    private SysRoleService sysRoleService;

    @BeforeEach
    void setUp() {
        // 只需要 roleMapper + listAll()，其余依赖传 null
        sysRoleService = new SysRoleService(roleMapper, null, null, null, (AuthorityApi) null);
        ReflectionTestUtils.setField(sysRoleService, "baseMapper", roleMapper);
    }

    @Test
    @DisplayName("listAll → 无数据应返回空列表")
    void shouldReturnEmptyListWhenNoData() {
        when(roleMapper.selectList(null)).thenReturn(List.of());

        List<RoleSimpleDTO> result = sysRoleService.listAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("listAll → 有数据应返回 id + name DTO")
    void shouldReturnIdAndNameDto() {
        SysRole r1 = new SysRole();
        r1.setId(1L);
        r1.setName("管理员");

        SysRole r2 = new SysRole();
        r2.setId(2L);
        r2.setName("业务员");

        when(roleMapper.selectList(null)).thenReturn(List.of(r1, r2));

        List<RoleSimpleDTO> result = sysRoleService.listAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("管理员");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("业务员");
    }
}
