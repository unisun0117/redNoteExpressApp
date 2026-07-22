package cn.qdm.tob.framework.jackson;

import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson 模块 —— 注册 {@link SensitiveSerializerModifier}，激活 {@code @Sensitive} 注解的处理。
 */
public class SensitiveJacksonModule extends SimpleModule {

    public SensitiveJacksonModule() {
        super(SensitiveJacksonModule.class.getName());
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializerModifier(new SensitiveSerializerModifier());
        super.setupModule(context);
    }
}
