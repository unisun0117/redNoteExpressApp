package cn.qdm.tob.modules.system.auth.config;

import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.infrastructure.security.RequirePermissionAuthorizationManager;
import cn.qdm.tob.modules.system.auth.security.JwtAuthenticationFilter;
import cn.qdm.tob.modules.system.auth.service.TokenProvider;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import cn.qdm.tob.modules.system.auth.service.TokenBlacklistService;
import cn.qdm.tob.modules.system.operator.service.OperatorStatusFilterService;
import cn.qdm.tob.modules.system.rbac.api.internal.AuthorityApi;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.method.AuthorizationInterceptorsOrder;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 配置
 * 无状态 JWT 模式，按路由前缀区分认证策略
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public AuthenticationManager authenticationManager(List<AuthenticationProvider> providers) {
        return new ProviderManager(providers);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(TokenProvider tokenProvider,
                                                            OperatorStatusFilterService statusFilterService,
                                                            TokenBlacklistService tokenBlacklistService,
                                                            AuthorityApi authorityApi,
                                                            JsonMapper jsonMapper) {
        return new JwtAuthenticationFilter(tokenProvider, statusFilterService, tokenBlacklistService, authorityApi, jsonMapper);
    }

    /**
     * {@link RequirePermission} 注解的方法拦截器。
     * 仅匹配带注解的方法/类，通过 {@link RequirePermissionAuthorizationManager} 校验权限码。
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static AuthorizationManagerBeforeMethodInterceptor requirePermissionInterceptor() {
        AuthorizationManager<MethodInvocation> manager = new RequirePermissionAuthorizationManager();
        Pointcut pointcut = new AnnotationMatchingPointcut(RequirePermission.class, RequirePermission.class, true);
        AuthorizationManagerBeforeMethodInterceptor interceptor = new AuthorizationManagerBeforeMethodInterceptor(pointcut, manager);
        interceptor.setOrder(AuthorizationInterceptorsOrder.PRE_AUTHORIZE.getOrder());
        return interceptor;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of(HttpHeaders.AUTHORIZATION));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 登录入口 → 完全公开
                        .requestMatchers("/api/admin/auth/**").permitAll()
                        .requestMatchers("/api/mall/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        // 错误页面 → 公开（否则 Controller 异常会被伪装成 403）
                        .requestMatchers("/error").permitAll()
                        // 健康检查、API 文档 → 完全公开
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/scalar/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        // 运营后台 API：
                        //   当前仅要求登录（.authenticated()），权限校验走
                        //   @RequirePermission 注解 + RequirePermissionAuthorizationManager。
                        //   后续如需启用动态 URL 匹配，切换为：
                        //   .requestMatchers("/api/admin/**").access(dynamicAuthorizationManager)
                        .requestMatchers("/api/admin/**").authenticated()
                        // 外部系统 → 仅认证（后续叠加签名校验）
                        .requestMatchers("/api/external/**").authenticated()
                        // 微信支付回调 → 由微信服务器调用，无需认证
                        .requestMatchers("/api/mall/pay/notify").permitAll()
                        // 微信退款回调 → 由微信服务器调用，无需认证
                        .requestMatchers("/api/mall/refund/notify").permitAll()
                        // 小程序 → 仅 JWT 认证
                        .requestMatchers("/api/mall/**").authenticated()
                        // 其余拒绝
                        .anyRequest().denyAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
