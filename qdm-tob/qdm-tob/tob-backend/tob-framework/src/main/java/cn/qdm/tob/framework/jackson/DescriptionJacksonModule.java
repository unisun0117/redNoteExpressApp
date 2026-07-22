package cn.qdm.tob.framework.jackson;

import cn.qdm.tob.framework.description.DescriptionProvider;
import tools.jackson.databind.module.SimpleModule;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Jackson 模块 —— 注册 {@link DescriptionValueSerializerModifier}，激活 {@code @Description} 注解的处理。
 * <p>
 * 使用前需通过 {@link #addProvider(DescriptionProvider)} 注册至少一个 {@link DescriptionProvider}。
 * 多个提供者按注册顺序匹配，首个返回非 null 的生效。
 * <p>
 * Spring Boot 自动装配：
 * <pre>{@code
 * @Bean
 * public DescriptionJacksonModule descriptionJacksonModule(List<DescriptionProvider> providers) {
 *     DescriptionJacksonModule module = new DescriptionJacksonModule();
 *     providers.forEach(module::addProvider);
 *     return module;
 * }
 * }</pre>
 */
public class DescriptionJacksonModule extends SimpleModule {

    private final List<DescriptionProvider> providers = new ArrayList<>();

    public DescriptionJacksonModule() {
        super(DescriptionJacksonModule.class.getName());
    }

    /** 注册一个描述提供者。提供者为 null 时忽略。 */
    public DescriptionJacksonModule addProvider(@Nullable DescriptionProvider provider) {
        if (Objects.nonNull(provider)) {
            providers.add(provider);
        }
        return this;
    }

    public List<DescriptionProvider> getProviders() {
        return Collections.unmodifiableList(providers);
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializerModifier(new DescriptionValueSerializerModifier(providers));
        super.setupModule(context);
    }
}
