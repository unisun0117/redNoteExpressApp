package cn.qdm.tob.modules.system.sequence;

import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.modules.system.sequence.api.internal.dto.SequenceResult;
import cn.qdm.tob.modules.system.sequence.domain.SysSequence;
import cn.qdm.tob.modules.system.sequence.mapper.SysSequenceMapper;
import cn.qdm.tob.modules.system.sequence.service.SequenceServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("通用序列号 Service")
class SequenceServiceImplTest {

    @Mock
    private SysSequenceMapper sequenceMapper;

    private SequenceServiceImpl sequenceService;

    @BeforeEach
    void setUp() {
        sequenceService = new SequenceServiceImpl();
        ReflectionTestUtils.setField(sequenceService, "baseMapper", sequenceMapper);
    }

    @Test
    @DisplayName("next → 序列不存在应抛出异常")
    void shouldThrowWhenSeqKeyNotFound() {
        when(sequenceMapper.findBySeqKey("invalid_key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sequenceService.next("invalid_key"))
                .isInstanceOf(TobServiceException.class)
                .hasMessageContaining("序列不存在");
    }

    @Test
    @DisplayName("next → 无 formatter 应返回原始数字")
    void shouldReturnRawValueWhenNoFormatter() {
        SysSequence seq = buildSequence("user_code", 5L, 1, null);
        when(sequenceMapper.findBySeqKey("user_code"))
                .thenReturn(Optional.of(seq))
                .thenReturn(Optional.of(buildSequence("user_code", 6L, 1, null)));

        SequenceResult result = sequenceService.next("user_code");

        assertThat(result.getSeqKey()).isEqualTo("user_code");
        assertThat(result.getRawValue()).isEqualTo(6L);
        assertThat(result.getFormattedValue()).isEqualTo("6");
        verify(sequenceMapper).incrementAndGet("user_code");
    }

    @Test
    @DisplayName("next → 有 formatter 应按格式返回")
    void shouldFormatValueWhenFormatterPresent() {
        SysSequence seq = buildSequence("order_no", 100L, 1, "ORD%06d");
        when(sequenceMapper.findBySeqKey("order_no"))
                .thenReturn(Optional.of(seq))
                .thenReturn(Optional.of(buildSequence("order_no", 101L, 1, "ORD%06d")));

        SequenceResult result = sequenceService.next("order_no");

        assertThat(result.getSeqKey()).isEqualTo("order_no");
        assertThat(result.getRawValue()).isEqualTo(101L);
        assertThat(result.getFormattedValue()).isEqualTo("ORD000101");
        verify(sequenceMapper).incrementAndGet("order_no");
    }

    @Test
    @DisplayName("next → 每次调用 rawValue 递增 step 值")
    void shouldIncrementByStepEachCall() {
        SysSequence seq = buildSequence("visit_count", 10L, 5, null);
        // 第一次调用后 current_val = 15
        when(sequenceMapper.findBySeqKey("visit_count"))
                .thenReturn(Optional.of(seq))
                .thenReturn(Optional.of(buildSequence("visit_count", 15L, 5, null)))
                .thenReturn(Optional.of(buildSequence("visit_count", 15L, 5, null)))
                .thenReturn(Optional.of(buildSequence("visit_count", 20L, 5, null)));

        SequenceResult first = sequenceService.next("visit_count");
        assertThat(first.getRawValue()).isEqualTo(15L);

        SequenceResult second = sequenceService.next("visit_count");
        assertThat(second.getRawValue()).isEqualTo(20L);

        verify(sequenceMapper, times(2)).incrementAndGet("visit_count");
    }

    @Test
    @DisplayName("next → 带 formatter 且 step 大于 1 应正确格式化")
    void shouldFormatWithStepGtOne() {
        SysSequence seq = buildSequence("coupon", 995L, 5, "C%04d");
        when(sequenceMapper.findBySeqKey("coupon"))
                .thenReturn(Optional.of(seq))
                .thenReturn(Optional.of(buildSequence("coupon", 1000L, 5, "C%04d")));

        SequenceResult result = sequenceService.next("coupon");

        assertThat(result.getRawValue()).isEqualTo(1000L);
        assertThat(result.getFormattedValue()).isEqualTo("C1000");
    }

    @Test
    @DisplayName("next → formatter 为空字符串应返回原始数字")
    void shouldReturnRawValueWhenFormatterIsEmptyString() {
        SysSequence seq = buildSequence("empty_fmt", 3L, 1, "");
        when(sequenceMapper.findBySeqKey("empty_fmt"))
                .thenReturn(Optional.of(seq))
                .thenReturn(Optional.of(buildSequence("empty_fmt", 4L, 1, "")));

        SequenceResult result = sequenceService.next("empty_fmt");

        assertThat(result.getFormattedValue()).isEqualTo("4");
    }

    private SysSequence buildSequence(String seqKey, Long currentVal, Integer step, String formatter) {
        SysSequence seq = new SysSequence();
        seq.setId(1L);
        seq.setSeqKey(seqKey);
        seq.setCurrentVal(currentVal);
        seq.setStep(step);
        seq.setFormatter(formatter);
        seq.setDescription("测试序列");
        seq.setCreatedAt(LocalDateTime.now());
        seq.setUpdatedAt(LocalDateTime.now());
        return seq;
    }
}
