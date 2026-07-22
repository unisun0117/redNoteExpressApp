package cn.qdm.tob.modules.system.operator;

import cn.qdm.tob.modules.system.operator.domain.SysOperator;
import cn.qdm.tob.modules.system.operator.domain.SysOperatorRegion;
import cn.qdm.tob.modules.system.operator.mapper.SysOperatorMapper;
import cn.qdm.tob.modules.system.operator.mapper.SysOperatorRegionMapper;
import cn.qdm.tob.modules.system.operator.service.OperatorRegionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("运营人员-销售大区绑定 Service（region_code）")
class OperatorRegionServiceTest {

    @Mock
    private SysOperatorRegionMapper operatorRegionMapper;

    @Mock
    private SysOperatorMapper sysOperatorMapper;

    private OperatorRegionService operatorRegionService;

    @BeforeEach
    void setUp() {
        operatorRegionService = new OperatorRegionService(sysOperatorMapper);
        ReflectionTestUtils.setField(operatorRegionService, "baseMapper", operatorRegionMapper);
    }

    @Test
    @DisplayName("getRegionCodes → 无绑定应返回空列表")
    void shouldReturnEmptyListWhenNoBindings() {
        when(operatorRegionMapper.findByOperatorId(1L)).thenReturn(List.of());

        List<String> result = operatorRegionService.getRegionCodes(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getRegionCodes → 有绑定应返回 regionCode 列表")
    void shouldReturnRegionCodes() {
        when(operatorRegionMapper.findByOperatorId(1L)).thenReturn(List.of(
                new SysOperatorRegion(1L, "HN001", null, null),
                new SysOperatorRegion(1L, "HD002", null, null)
        ));

        List<String> result = operatorRegionService.getRegionCodes(1L);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("HN001", "HD002");
    }

    @Test
    @DisplayName("setRegions → 用户不存在应抛出异常")
    void shouldThrowWhenOperatorNotFound() {
        when(sysOperatorMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> operatorRegionService.setRegions(99L, Set.of("HN001")))
                .hasMessage("用户不存在");

        verify(operatorRegionMapper, never()).deleteByOperatorId(any());
        verify(operatorRegionMapper, never()).insert((SysOperatorRegion) any());
    }

    @Test
    @DisplayName("setRegions → 覆盖式绑定成功")
    void shouldOverrideBindings() {
        SysOperator operator = new SysOperator();
        operator.setId(1L);
        when(sysOperatorMapper.selectById(1L)).thenReturn(operator);

        operatorRegionService.setRegions(1L, Set.of("HN001", "HD002", "XN003"));

        verify(operatorRegionMapper).deleteByOperatorId(1L);
        verify(operatorRegionMapper, times(3)).insert((SysOperatorRegion) any());
    }

    @Test
    @DisplayName("setRegions → 传入空集合应仅删除旧绑定")
    void shouldJustDeleteWhenEmptySet() {
        SysOperator operator = new SysOperator();
        operator.setId(1L);
        when(sysOperatorMapper.selectById(1L)).thenReturn(operator);

        operatorRegionService.setRegions(1L, Set.of());

        verify(operatorRegionMapper).deleteByOperatorId(1L);
        verify(operatorRegionMapper, never()).insert((SysOperatorRegion) any());
    }
}
