package cn.qdm.tob.infrastructure.security;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.apache.commons.lang3.ArrayUtils;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 将 {@link RequirePermission} 注解的权限信息自动注入到 OpenAPI 规范的 {@code security} 字段，
 * 使 Scalar UI 能够展示每个接口所需的权限码和中文描述。
 *
 * <p>解析规则：方法级优先，无则回退到类级别；无注解的方法不做处理。</p>
 */
public class RequirePermissionOpenApiCustomizer implements GlobalOperationCustomizer {

    /** OpenAPI SecurityScheme 的注册名称，需与 {@code OpenApiCustomizer} 中注册的一致 */
    public static final String SECURITY_SCHEME_NAME = "接口权限";

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Permissions[] permissions = resolveRequiredPermissions(handlerMethod);
        if (ArrayUtils.isEmpty(permissions)) return operation;

        List<String> list = Arrays
                .stream(permissions)
                .map(p -> p.getValue() + " (" + p.getDescription() + ")")
                .toList();

        operation.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME, list));
        return operation;
    }

    /**
     * 解析权限注解：方法级优先，类级别回退。
     */
    private Permissions[] resolveRequiredPermissions(HandlerMethod handlerMethod) {
        RequirePermission ann = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (Objects.nonNull(ann)) {
            return ann.value();
        }
        ann = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        if (Objects.nonNull(ann)) {
            return ann.value();
        }
        return new Permissions[0];
    }
}
