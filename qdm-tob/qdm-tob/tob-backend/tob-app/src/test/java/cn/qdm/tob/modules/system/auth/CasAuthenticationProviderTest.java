package cn.qdm.tob.modules.system.auth;

import cn.qdm.tob.framework.exception.AuthenticationException;
import cn.qdm.tob.modules.system.auth.provider.CasAuthenticationProvider;
import cn.qdm.tob.modules.system.auth.token.CasAuthenticationToken;
import cn.qdm.tob.modules.system.operator.enums.OperatorStatus;
import cn.qdm.tob.modules.system.operator.enums.OperatorType;
import cn.qdm.tob.modules.system.operator.domain.SysOperator;
import cn.qdm.tob.modules.system.operator.mapper.SysOperatorMapper;
import cn.qdm.tob.modules.system.rbac.service.OperatorAuthorityLoader;
import org.apereo.cas.client.authentication.AttributePrincipal;
import org.apereo.cas.client.validation.Assertion;
import org.apereo.cas.client.validation.TicketValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CAS 认证 Provider")
class CasAuthenticationProviderTest {

    @Mock
    private SysOperatorMapper operatorMapper;

    @Mock
    private OperatorAuthorityLoader authorityLoader;

    @Mock
    private TicketValidator ticketValidator;

    @Mock
    private Assertion assertion;

    @Mock
    private AttributePrincipal attributePrincipal;

    private CasAuthenticationProvider provider;

    private static final String TICKET = "ST-test-ticket";
    private static final String SERVICE = "https://example.com/home";
    private static final String EMPLOYEE_CODE = "10086";
    private static final String NAME_PINYIN = "zhangsan";

    @BeforeEach
    void setUp() {
        provider = new CasAuthenticationProvider(operatorMapper, authorityLoader, "https://cas.test.cn");
        ReflectionTestUtils.setField(provider, "ticketValidator", ticketValidator);
    }

    @Test
    @DisplayName("employeeCode 匹配成功 → 返回已认证 Token")
    void shouldAuthenticateWhenEmployeeCodeMatches() throws Exception {
        SysOperator operator = buildOperator(EMPLOYEE_CODE, NAME_PINYIN);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("employeeCode", EMPLOYEE_CODE);
        attrs.put("namePinyin", NAME_PINYIN);

        when(ticketValidator.validate(TICKET, SERVICE)).thenReturn(assertion);
        when(assertion.getPrincipal()).thenReturn(attributePrincipal);
        when(attributePrincipal.getAttributes()).thenReturn(attrs);
        when(operatorMapper.findByEmployeeCode(EMPLOYEE_CODE)).thenReturn(Optional.of(operator));
        when(authorityLoader.loadAuthorities(operator.getId())).thenReturn(Collections.emptyList());
        when(operatorMapper.updateById(any(SysOperator.class))).thenReturn(1);

        Authentication result = provider.authenticate(new CasAuthenticationToken(TICKET, SERVICE));

        assertThat(result).isNotNull();
        assertThat(result.isAuthenticated()).isTrue();
        verify(operatorMapper).findByEmployeeCode(EMPLOYEE_CODE);
    }

    @Test
    @DisplayName("employeeCode 匹配 + loginId 为空 → auto-fill namePinyin")
    void shouldAutoFillLoginIdWhenEmpty() throws Exception {
        SysOperator operator = buildOperator(EMPLOYEE_CODE, null);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("employeeCode", EMPLOYEE_CODE);
        attrs.put("namePinyin", NAME_PINYIN);

        when(ticketValidator.validate(TICKET, SERVICE)).thenReturn(assertion);
        when(assertion.getPrincipal()).thenReturn(attributePrincipal);
        when(attributePrincipal.getAttributes()).thenReturn(attrs);
        when(operatorMapper.findByEmployeeCode(EMPLOYEE_CODE)).thenReturn(Optional.of(operator));
        when(authorityLoader.loadAuthorities(operator.getId())).thenReturn(Collections.emptyList());
        when(operatorMapper.updateById(any(SysOperator.class))).thenReturn(1);

        provider.authenticate(new CasAuthenticationToken(TICKET, SERVICE));

        assertThat(operator.getLoginId()).isEqualTo(NAME_PINYIN);
        verify(operatorMapper, atLeastOnce()).updateById(any(SysOperator.class));
    }

    @Test
    @DisplayName("employeeCode 匹配 → lastLoginAt 已更新")
    void shouldUpdateLastLoginAt() throws Exception {
        SysOperator operator = buildOperator(EMPLOYEE_CODE, NAME_PINYIN);
        operator.setLastLoginAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("employeeCode", EMPLOYEE_CODE);

        when(ticketValidator.validate(TICKET, SERVICE)).thenReturn(assertion);
        when(assertion.getPrincipal()).thenReturn(attributePrincipal);
        when(attributePrincipal.getAttributes()).thenReturn(attrs);
        when(operatorMapper.findByEmployeeCode(EMPLOYEE_CODE)).thenReturn(Optional.of(operator));
        when(authorityLoader.loadAuthorities(operator.getId())).thenReturn(Collections.emptyList());
        when(operatorMapper.updateById(any(SysOperator.class))).thenReturn(1);

        provider.authenticate(new CasAuthenticationToken(TICKET, SERVICE));

        assertThat(operator.getLastLoginAt()).isAfter(LocalDateTime.of(2025, 1, 1, 0, 0));
    }

    @Test
    @DisplayName("employeeCode 不匹配 → 403")
    void shouldThrow403WhenNotFound() throws Exception {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("employeeCode", "unknown");

        when(ticketValidator.validate(TICKET, SERVICE)).thenReturn(assertion);
        when(assertion.getPrincipal()).thenReturn(attributePrincipal);
        when(attributePrincipal.getAttributes()).thenReturn(attrs);
        when(operatorMapper.findByEmployeeCode("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> provider.authenticate(new CasAuthenticationToken(TICKET, SERVICE)))
                .isInstanceOf(AuthenticationException.class);

        verify(operatorMapper, never()).insert(any(SysOperator.class));
    }

    @Test
    @DisplayName("不再自动建号 → mapper.insert 从未调用")
    void shouldNeverAutoCreateOperator() throws Exception {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("employeeCode", "new_guy");

        when(ticketValidator.validate(TICKET, SERVICE)).thenReturn(assertion);
        when(assertion.getPrincipal()).thenReturn(attributePrincipal);
        when(attributePrincipal.getAttributes()).thenReturn(attrs);
        when(operatorMapper.findByEmployeeCode("new_guy")).thenReturn(Optional.empty());

        try {
            provider.authenticate(new CasAuthenticationToken(TICKET, SERVICE));
        } catch (AuthenticationException ignored) {
            // expected
        }

        verify(operatorMapper, never()).insert(any(SysOperator.class));
    }

    @Test
    @DisplayName("无 employeeCode 属性 → fallback 用 principal name 匹配")
    void shouldFallbackToPrincipalNameWhenEmployeeCodeMissing() throws Exception {
        String principalName = "13631406762";
        SysOperator operator = buildOperator(principalName, NAME_PINYIN);
        Map<String, Object> attrs = new HashMap<>();

        when(ticketValidator.validate(TICKET, SERVICE)).thenReturn(assertion);
        when(assertion.getPrincipal()).thenReturn(attributePrincipal);
        when(attributePrincipal.getAttributes()).thenReturn(attrs);
        when(attributePrincipal.getName()).thenReturn(principalName);
        when(operatorMapper.findByEmployeeCode(principalName)).thenReturn(Optional.of(operator));
        when(authorityLoader.loadAuthorities(operator.getId())).thenReturn(Collections.emptyList());
        when(operatorMapper.updateById(any(SysOperator.class))).thenReturn(1);

        Authentication result = provider.authenticate(new CasAuthenticationToken(TICKET, SERVICE));

        assertThat(result).isNotNull();
        assertThat(result.isAuthenticated()).isTrue();
        verify(operatorMapper).findByEmployeeCode(principalName);
    }

    private SysOperator buildOperator(String employeeCode, String loginId) {
        SysOperator op = new SysOperator();
        op.setId(1L);
        op.setEmployeeCode(employeeCode);
        op.setLoginId(loginId);
        op.setMobile("13800000000");
        op.setRealName("测试");
        op.setType(OperatorType.SALESMAN);
        op.setStatus(OperatorStatus.ACTIVE);
        op.setCreatedAt(LocalDateTime.now());
        op.setUpdatedAt(LocalDateTime.now());
        return op;
    }
}
