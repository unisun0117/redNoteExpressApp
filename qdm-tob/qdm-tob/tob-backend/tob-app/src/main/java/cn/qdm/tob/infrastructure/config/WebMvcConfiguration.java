package cn.qdm.tob.infrastructure.config;

import cn.qdm.tob.infrastructure.security.resolver.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 全局配置 — 注册自定义参数解析器
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final List<HandlerMethodArgumentResolver> resolvers;

    public WebMvcConfiguration(List<HandlerMethodArgumentResolver> resolvers) {
        this.resolvers = resolvers;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> registry) {
        registry.add(new CurrentUserArgumentResolver());
        registry.addAll(resolvers);
    }
}
