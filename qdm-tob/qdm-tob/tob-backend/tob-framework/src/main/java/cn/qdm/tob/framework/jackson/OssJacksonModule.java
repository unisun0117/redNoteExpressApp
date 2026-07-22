package cn.qdm.tob.framework.jackson;

import cn.qdm.tob.framework.oss.OssUrlGenerator;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson 模块 —— 注册 {@link OssResourceSerializerModifier}，激活 {@code @OssResource} 注解的处理。
 */
@RequiredArgsConstructor
public class OssJacksonModule extends SimpleModule {

    private final OssUrlGenerator ossUrlGenerator;

    public OssJacksonModule() {
        super(OssJacksonModule.class.getName());
        this.ossUrlGenerator = null;
    }

    @Override
    public void setupModule(SetupContext context) {
        if (ossUrlGenerator != null) {
            context.addSerializerModifier(new OssResourceSerializerModifier(ossUrlGenerator));
        }
        super.setupModule(context);
    }
}
