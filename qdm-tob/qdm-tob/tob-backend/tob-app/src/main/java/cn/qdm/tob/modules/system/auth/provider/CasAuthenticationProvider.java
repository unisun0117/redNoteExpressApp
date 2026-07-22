package cn.qdm.tob.modules.system.auth.provider;

import cn.qdm.tob.framework.exception.AuthenticationException;
import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.modules.system.operator.domain.SysOperator;
import cn.qdm.tob.modules.system.auth.enums.AuthType;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.modules.system.auth.token.CasAuthenticationToken;
import cn.qdm.tob.modules.system.operator.mapper.SysOperatorMapper;
import cn.qdm.tob.modules.system.rbac.service.OperatorAuthorityLoader;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.client.validation.Assertion;
import org.apereo.cas.client.validation.Cas30ServiceTicketValidator;
import org.apereo.cas.client.validation.TicketValidationException;
import org.apereo.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * CAS 认证 Provider（统一 PC 后台 + 企微 H5 登录入口）
 *
 * <p>认证流程:
 * <ol>
 *   <li>CAS 3.0 验证 ticket（/p3/serviceValidate），取 assertion.attributes</li>
 *   <li>按 employeeCode 匹配 operator</li>
 *   <li>匹配不到则按 mobile 匹配，校验工号一致性</li>
 *   <li>更新 loginId 和 lastLoginAt</li>
 *   <li>加载权限 → 返回已认证 Token</li>
 * </ol>
 */
@Slf4j
@Component
public class CasAuthenticationProvider implements AuthenticationProvider {

    private final SysOperatorMapper sysOperatorMapper;
    private final OperatorAuthorityLoader authorityLoader;
    private final TicketValidator ticketValidator;

    public CasAuthenticationProvider(SysOperatorMapper sysOperatorMapper,
                                     OperatorAuthorityLoader authorityLoader,
                                     @Value("${app.auth.cas.server-url:}") String casServerUrl) {
        this.sysOperatorMapper = sysOperatorMapper;
        this.authorityLoader = authorityLoader;
        this.ticketValidator = new Cas30ServiceTicketValidator(casServerUrl);
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        CasAuthenticationToken token = (CasAuthenticationToken) authentication;
        String ticket = (String) token.getPrincipal();
        String service = token.getService();

        log.info("CAS 登录开始: ticket={}, service={}", ticket, service);

        // 1. CAS 3.0 验证 ticket → 提取属性
        // CAS 3.0 的 <cas:attributes> 挂在 AttributePrincipal 上，不是 Assertion 上
        String employeeCode;
        String mobile;
        Map<String, Object> principalAttrs;
        try {
            Assertion assertion = ticketValidator.validate(ticket, service);
            principalAttrs = assertion.getPrincipal().getAttributes();
            log.info("CAS 验证成功: attrs={}", principalAttrs);
            Object empCodeAttr = principalAttrs.get("employeeCode");
            employeeCode = empCodeAttr != null ? empCodeAttr.toString() : assertion.getPrincipal().getName();
            Object mobileAttr = principalAttrs.get("telphone");
            mobile = mobileAttr != null ? mobileAttr.toString() : null;
        } catch (TicketValidationException e) {
            log.error("CAS ticket 验证失败: ticket={}", ticket, e);
            throw new AuthenticationException(ErrorCode.UNAUTHORIZED.code(), "CAS 票据验证失败: " + e.getMessage());
        }

        // 2. 按 employeeCode 查 operator；查不到则按 mobile 查（校验工号一致）
        SysOperator admin = sysOperatorMapper.findByEmployeeCode(employeeCode).orElseGet(() -> findByMobile(mobile, employeeCode));

        // 3. login_id 为空时自动补充 namePinyin
        if (admin.getLoginId() == null || admin.getLoginId().isEmpty()) {
            Object namePinyin = principalAttrs.get("namePinyin");
            if (namePinyin != null) {
                admin.setLoginId(namePinyin.toString());
                log.info("Auto-filled login_id for operator {}: {}", admin.getId(), admin.getLoginId());
            }
        }

        // 4. 更新最后登录时间
        admin.setLastLoginAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        admin.setUpdatedBy(admin.getRealName());
        sysOperatorMapper.updateById(admin);

        // 5. 加载权限
        Collection<GrantedAuthority> authorities = authorityLoader.loadAuthorities(admin.getId());

        // 6. 构造已认证 Token
        UserPrincipal principal = new UserPrincipal(
                admin.getId(), AuthType.CAS.toString(), admin.getMobile(), admin.getRealName(), authorities);
        principal.setEmployeeCode(employeeCode);

        log.info("CAS login success: employeeCode={}, operatorId={}", employeeCode, admin.getId());
        return new CasAuthenticationToken(principal, authorities);
    }

    /** 按手机号匹配 operator，校验工号一致性 */
    private SysOperator findByMobile(String mobile, String employeeCode) {
        if (mobile == null || mobile.isEmpty()) {
            log.warn("Operator not found for employeeCode: {}, 且无 mobile 可匹配", employeeCode);
            throw new AuthenticationException(ErrorCode.FORBIDDEN.code(), "用户不存在，请联系管理员创建账号");
        }

        Optional<SysOperator> byMobile = sysOperatorMapper.findByMobile(mobile);
        if (byMobile.isEmpty()) {
            log.warn("Operator not found for employeeCode: {}, mobile: {}", employeeCode, mobile);
            throw new AuthenticationException(ErrorCode.FORBIDDEN.code(), "用户不存在，请联系管理员创建账号");
        }

        SysOperator op = byMobile.get();
        if (op.getEmployeeCode() != null && !op.getEmployeeCode().equals(employeeCode)) {
            log.warn("手机号已绑定其他工号: mobile={}, 系统中工号={}, CAS工号={}", mobile, op.getEmployeeCode(), employeeCode);
            throw new AuthenticationException(ErrorCode.FORBIDDEN.code(),
                    "该手机号已绑定工号 " + op.getEmployeeCode() + "，与当前登录工号 " + employeeCode + " 不一致，请联系管理员");
        }

        log.info("手机号匹配成功: mobile={}, operatorId={}, employeeCode={}", mobile, op.getId(), employeeCode);
        return op;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CasAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
