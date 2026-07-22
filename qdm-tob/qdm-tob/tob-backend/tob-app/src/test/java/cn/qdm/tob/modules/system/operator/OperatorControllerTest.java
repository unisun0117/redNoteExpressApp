package cn.qdm.tob.modules.system.operator;

import cn.qdm.tob.modules.system.operator.enums.OperatorStatus;
import cn.qdm.tob.modules.system.operator.enums.OperatorType;
import cn.qdm.tob.modules.system.operator.domain.SysOperator;
import cn.qdm.tob.modules.system.operator.dto.OperatorSaveDTO;
import cn.qdm.tob.modules.system.operator.mapper.SysOperatorMapper;
import cn.qdm.tob.modules.system.operator.service.OperatorStatusFilterService;
import cn.qdm.tob.modules.system.operator.service.impl.OperatorServiceImpl;
import cn.qdm.tob.modules.system.sequence.api.internal.SequenceApi;
import cn.qdm.tob.modules.system.sequence.api.internal.dto.SequenceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Operator CRUD Service")
class OperatorControllerTest {

    @Mock
    private SysOperatorMapper operatorMapper;

    @Mock
    private OperatorMapping operatorMapping;

    @Mock
    private OperatorStatusFilterService statusFilterService;

    @Mock
    private SequenceApi sequenceApi;

    private OperatorServiceImpl operatorService;

    @BeforeEach
    void setUp() {
        operatorService = new OperatorServiceImpl(operatorMapping, statusFilterService, sequenceApi);
        ReflectionTestUtils.setField(operatorService, "baseMapper", operatorMapper);
    }

    // ===== 新增 =====

    @Test
    @DisplayName("新增 → employeeCode 重复应报错")
    void shouldFailWhenEmployeeCodeDuplicate() {
        OperatorSaveDTO dto = buildSaveDTO("1001", "张三", "13800001111");
        when(operatorMapper.findByEmployeeCode("1001"))
                .thenReturn(Optional.of(buildOperator(1L, "1001")));

        assertThatThrownBy(() -> operatorService.create(dto))
                .isInstanceOf(cn.qdm.tob.framework.exception.TobServiceException.class)
                .hasMessageContaining("工号已存在");
    }

    @Test
    @DisplayName("新增 → mobile 重复应报错")
    void shouldFailWhenMobileDuplicate() {
        OperatorSaveDTO dto = buildSaveDTO("1002", "李四", "13800001111");
        when(operatorMapper.findByEmployeeCode("1002")).thenReturn(Optional.empty());
        when(operatorMapper.findByMobile("13800001111"))
                .thenReturn(Optional.of(buildOperator(3L, "1003")));

        assertThatThrownBy(() -> operatorService.create(dto))
                .isInstanceOf(cn.qdm.tob.framework.exception.TobServiceException.class)
                .hasMessageContaining("手机号已被其他用户使用");
    }

    @Test
    @DisplayName("新增 → 默认状态为 ACTIVE + 自动生成推荐码")
    void shouldInsertWithDefaultStatus() {
        OperatorSaveDTO dto = buildSaveDTO("1005", "王五", "13855556666");
        dto.setStatus(null);
        when(operatorMapper.findByEmployeeCode("1005")).thenReturn(Optional.empty());
        when(operatorMapper.findByMobile("13855556666")).thenReturn(Optional.empty());
        when(operatorMapping.toEntity(dto)).thenReturn(toEntity(dto));
        when(sequenceApi.next("USER_RECOMMEND_CODE"))
                .thenReturn(new SequenceResult("USER_RECOMMEND_CODE", "0001", 1L));
        when(operatorMapper.insert(any(SysOperator.class))).thenReturn(1);

        operatorService.create(dto);

        verify(operatorMapper).insert(argThat((SysOperator op) ->
                op.getStatus() == OperatorStatus.ACTIVE
                && "王五".equals(op.getRealName())
                && "0001".equals(op.getRecommendCode())));
    }

    @Test
    @DisplayName("新增 → 自动调用 SequenceApi 生成推荐码")
    void shouldCallSequenceApiForRecommendCode() {
        OperatorSaveDTO dto = buildSaveDTO("1006", "赵六", "13899990000");
        when(operatorMapper.findByEmployeeCode("1006")).thenReturn(Optional.empty());
        when(operatorMapper.findByMobile("13899990000")).thenReturn(Optional.empty());
        when(operatorMapping.toEntity(dto)).thenReturn(toEntity(dto));
        when(sequenceApi.next("USER_RECOMMEND_CODE"))
                .thenReturn(new SequenceResult("USER_RECOMMEND_CODE", "0042", 42L));
        when(operatorMapper.insert(any(SysOperator.class))).thenReturn(1);

        operatorService.create(dto);

        verify(sequenceApi).next("USER_RECOMMEND_CODE");
    }

    // ===== 编辑 =====

    @Test
    @DisplayName("编辑 → mobile 唯一性排除自身")
    void shouldAllowSameMobileOnSelfEdit() {
        OperatorSaveDTO dto = buildSaveDTO(null, "张三改", "13800001111");
        SysOperator existing = buildOperator(1L, "1001");
        existing.setMobile("13800001111");

        when(operatorMapper.selectById(1L)).thenReturn(existing);
        when(operatorMapper.findByMobile("13800001111")).thenReturn(Optional.of(existing));
        when(operatorMapper.updateById(any(SysOperator.class))).thenReturn(1);

        operatorService.update(1L, dto);

        verify(operatorMapper).updateById(argThat((SysOperator op) ->
                "张三改".equals(op.getRealName())));
    }

    @Test
    @DisplayName("编辑 → employeeCode 不接受修改")
    void shouldNotChangeEmployeeCodeOnEdit() {
        SysOperator existing = buildOperator(2L, "1006");
        OperatorSaveDTO dto = buildSaveDTO("illegal", "改名", "13866667777");

        when(operatorMapper.selectById(2L)).thenReturn(existing);
        when(operatorMapper.findByMobile("13866667777")).thenReturn(Optional.empty());
        when(operatorMapper.updateById(any(SysOperator.class))).thenReturn(1);

        operatorService.update(2L, dto);

        assertThat(existing.getEmployeeCode()).isEqualTo("1006");
    }

    // ===== 启用/停用 =====

    @Test
    @DisplayName("停用 → SADD 黑名单")
    void shouldAddBlacklistOnInactivate() {
        SysOperator existing = buildOperator(1L, "1001");
        when(operatorMapper.selectById(1L)).thenReturn(existing);
        when(operatorMapper.updateById(any(SysOperator.class))).thenReturn(1);

        operatorService.updateStatus(1L, "INACTIVE");

        assertThat(existing.getStatus()).isEqualTo(OperatorStatus.INACTIVE);
        verify(statusFilterService).addToBlacklist(1L);
    }

    @Test
    @DisplayName("启用 → SREM 黑名单")
    void shouldRemoveBlacklistOnActivate() {
        SysOperator existing = buildOperator(1L, "1001");
        existing.setStatus(OperatorStatus.INACTIVE);
        when(operatorMapper.selectById(1L)).thenReturn(existing);
        when(operatorMapper.updateById(any(SysOperator.class))).thenReturn(1);

        operatorService.updateStatus(1L, "ACTIVE");

        assertThat(existing.getStatus()).isEqualTo(OperatorStatus.ACTIVE);
        verify(statusFilterService).removeFromBlacklist(1L);
    }

    // ===== 手机号脱敏 =====

    @Test
    @DisplayName("手机号脱敏: 13812345678 → 138****5678")
    void shouldMaskMobile() {
        assertThat(OperatorServiceImpl.maskMobile("13812345678")).isEqualTo("138****5678");
    }

    @Test
    @DisplayName("手机号脱敏: null → 空字符串")
    void shouldReturnEmptyForNullMobile() {
        assertThat(OperatorServiceImpl.maskMobile(null)).isEqualTo("");
    }

    // ===== 辅助 =====

    private OperatorSaveDTO buildSaveDTO(String employeeCode, String realName, String mobile) {
        OperatorSaveDTO dto = new OperatorSaveDTO();
        dto.setEmployeeCode(employeeCode);
        dto.setRealName(realName);
        dto.setMobile(mobile);
        dto.setType(OperatorType.SALESMAN);
        dto.setStatus(OperatorStatus.ACTIVE);
        return dto;
    }

    private SysOperator buildOperator(Long id, String employeeCode) {
        SysOperator op = new SysOperator();
        op.setId(id);
        op.setEmployeeCode(employeeCode);
        op.setMobile("13800000000");
        op.setRealName("测试");
        op.setType(OperatorType.SALESMAN);
        op.setStatus(OperatorStatus.ACTIVE);
        op.setCreatedAt(LocalDateTime.now());
        op.setUpdatedAt(LocalDateTime.now());
        return op;
    }

    private SysOperator toEntity(OperatorSaveDTO dto) {
        SysOperator op = new SysOperator();
        op.setEmployeeCode(dto.getEmployeeCode());
        op.setRealName(dto.getRealName());
        op.setMobile(dto.getMobile());
        op.setType(dto.getType());
        op.setStatus(dto.getStatus());
        return op;
    }
}
