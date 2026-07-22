package cn.qdm.tob.infrastructure.config;

import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.infrastructure.security.RequirePermissionOpenApiCustomizer;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    /**
     * 注册 OpenAPI SecurityScheme，配合 {@link RequirePermissionOpenApiCustomizer}
     * 在 Scalar UI 中以锁图标展示接口所需权限。
     */
    @Bean
    public OpenApiCustomizer requirePermissionSecuritySchemeCustomizer() {
        return openApi -> openApi.getComponents()
                .addSecuritySchemes(RequirePermissionOpenApiCustomizer.SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .name(RequirePermissionOpenApiCustomizer.SECURITY_SCHEME_NAME)
                                .in(SecurityScheme.In.HEADER));
    }/**
     * 自动读取 {@link RequirePermission} 注解，将权限信息写入 OpenAPI Operation 的 security 字段。
     */
    @Bean
    public GlobalOperationCustomizer requirePermissionOperationCustomizer() {
        return new RequirePermissionOpenApiCustomizer();
    }
}
