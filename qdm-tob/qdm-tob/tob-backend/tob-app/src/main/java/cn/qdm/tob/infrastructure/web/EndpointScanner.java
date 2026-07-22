package cn.qdm.tob.infrastructure.web;

import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.infrastructure.web.domain.EndpointPermission;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 端点扫描器 — 扫描所有已注册的 Spring MVC 端点，提取 URL pattern、HTTP 方法及所需权限。
 */
@Component
@RequiredArgsConstructor
public class EndpointScanner {

    private final RequestMappingHandlerMapping handlerMapping;

    /**
     * 扫描所有已注册端点，返回 URL pattern、HTTP 方法与所需权限码列表。
     */
    public List<EndpointPermission> scan() {
        List<EndpointPermission> result = new ArrayList<>();
        var handlerMethods = handlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mapping = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            String pattern = extractPattern(mapping);
            String method = extractMethod(mapping);
            String description = extractDescription(handlerMethod);
            String controllerAction = extractControllerAction(handlerMethod);
            List<String> authorities = resolveRequiredAuthorities(handlerMethod);

            result.add(new EndpointPermission(pattern, method, description, controllerAction, authorities));
        }

        return result;
    }

    /**
     * 提取 URL 模式（取第一个，绝大多数端点只有一个 URL）。
     */
    private String extractPattern(RequestMappingInfo mapping) {
        if (mapping.getPathPatternsCondition() != null) {
            return mapping.getPathPatternsCondition().getPatternValues().stream().findFirst().orElse("");
        }
        return mapping.getDirectPaths().stream().findFirst().orElse("");
    }

    /**
     * 提取 HTTP 方法（一个 entry 通常对应一个 method）。
     */
    private String extractMethod(RequestMappingInfo mapping) {
        return mapping.getMethodsCondition().getMethods().stream()
                .map(org.springframework.web.bind.annotation.RequestMethod::name)
                .findFirst()
                .orElse("*");
    }

    /**
     * 从 @Operation 注解提取接口描述，优先取 summary，为空则取 description。
     */
    private String extractDescription(HandlerMethod handlerMethod) {
        var method = handlerMethod.getMethod();
        Operation op = AnnotationUtils.findAnnotation(method, Operation.class);
        if (op == null) {
            op = AnnotationUtils.findAnnotation(method.getDeclaringClass(), Operation.class);
        }
        if (op == null) {
            return "";
        }
        return !op.summary().isBlank() ? op.summary() : op.description();
    }

    /**
     * 拼接 Controller 名称 + Action 名称，如 UserController#list。
     */
    private String extractControllerAction(HandlerMethod handlerMethod) {
        String controller = handlerMethod.getBeanType().getSimpleName();
        String action = handlerMethod.getMethod().getName();
        return controller + "#" + action;
    }

    /**
     * 从方法注解 → 类注解依次解析所需权限码，
     * 与 {@link cn.qdm.tob.infrastructure.security.RequirePermissionAuthorizationManager} 保持一致。
     */
    private List<String> resolveRequiredAuthorities(HandlerMethod handlerMethod) {
        var method = handlerMethod.getMethod();

        RequirePermission ann = AnnotationUtils.findAnnotation(method, RequirePermission.class);
        if (ann == null) {
            ann = AnnotationUtils.findAnnotation(method.getDeclaringClass(), RequirePermission.class);
        }

        if (ann == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(ann.value())
                .map(Permissions::getValue)
                .toList();
    }
}
