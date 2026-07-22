package cn.qdm.tob.modules.system.auth.security;

import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.modules.system.auth.enums.AuthType;
import cn.qdm.tob.modules.system.auth.service.TokenBlacklistService;
import cn.qdm.tob.modules.system.auth.service.TokenProvider;
import cn.qdm.tob.modules.system.auth.token.JwtAuthenticationToken;
import cn.qdm.tob.modules.system.operator.service.OperatorStatusFilterService;
import cn.qdm.tob.modules.system.rbac.api.internal.AuthorityApi;
import tools.jackson.databind.json.JsonMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 从 Authorization: Bearer {token} 解析 JWT，验证后从 Redis 加载权限写入 SecurityContext。
 * 无状态模式，每个请求重建 SecurityContext（含权限实时查询）。
 * 保留黑名单和停用校验。
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JsonMapper jsonMapper;

    private final TokenProvider tokenProvider;
    private final OperatorStatusFilterService statusFilterService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthorityApi authorityApi;

    public JwtAuthenticationFilter(TokenProvider tokenProvider,
                                   OperatorStatusFilterService statusFilterService,
                                   TokenBlacklistService tokenBlacklistService,
                                   AuthorityApi authorityApi,
                                   JsonMapper jsonMapper) {
        this.tokenProvider = tokenProvider;
        this.statusFilterService = statusFilterService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.authorityApi = authorityApi;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null) {
            try {
                UserPrincipal principal = tokenProvider.validateToken(token);

                // Token 黑名单校验（已退出登录的 token 不可复用）
                if (tokenBlacklistService.isBlacklisted(principal.getJti())) {
                    log.debug("Token 已登出，拒绝访问: jti={}", principal.getJti());
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                boolean isAdmin = AuthType.isAdmin(principal.getAuthType());

                // 停用黑名单校验（仅管理员）
                if (isAdmin && principal.getUserId() != null
                        && statusFilterService.isInactive(principal.getUserId())) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(jsonMapper.writeValueAsString(
                            ResponseResult.error(ErrorCode.FORBIDDEN)));
                    return;
                }

                // 从 Redis 加载权限（JWT 不再携带 aut claim），非管理员跳过
                Collection<GrantedAuthority> authorities = isAdmin
                        ? authorityApi.loadAuthorities(principal.getUserId())
                        : Collections.emptyList();

                JwtAuthenticationToken authentication = new JwtAuthenticationToken(principal, authorities);

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (Exception e) {
                log.debug("JWT validation failed: ", e);
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
